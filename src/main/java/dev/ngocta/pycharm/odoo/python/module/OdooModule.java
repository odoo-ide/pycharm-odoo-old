package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModule {
    private final PsiDirectory myDirectory;

    public OdooModule(@NotNull PsiDirectory directory) {
        myDirectory = directory;
    }

    @NotNull
    public String getName() {
        return myDirectory.getName();
    }

    @NotNull
    public Project getProject() {
        return myDirectory.getProject();
    }

    @NotNull
    public PsiDirectory getDirectory() {
        return myDirectory;
    }

    @Nullable
    public PsiFile getManifest() {
        return getDirectory().findFile(OdooNames.MANIFEST_FILE_NAME);
    }

    @Nullable
    public OdooManifestInfo getManifestInfo() {
        PsiFile manifest = getManifest();
        return manifest != null ? OdooManifestInfo.parseManifest(manifest) : null;
    }

    @NotNull
    public List<OdooModule> getDepends() {
        return PyUtil.getParameterizedCachedValue(getDirectory(), null, param -> {
            OdooManifestInfo info = getManifestInfo();
            if (info == null) {
                return Collections.emptyList();
            }
            List<OdooModule> result = new LinkedList<>();
            String[] depends = info.getDepends();
            if (depends == null) {
                return Collections.emptyList();
            }
            for (String depend : depends) {
                OdooModule module = OdooModuleIndex.getOdooModuleByName(depend, getDirectory());
                if (module != null) {
                    result.add(module);
                }
            }
            return result;
        });
    }

    @NotNull
    public List<OdooModule> getFlattenedDependsGraph() {
        List<OdooModule> visitedModules = new LinkedList<>();
        List<OdooModule> modules = new LinkedList<>();
        modules.add(this);
        OdooModule module;
        while (!modules.isEmpty()) {
            module = modules.remove(0);
            visitedModules.remove(module);
            visitedModules.add(module);
            modules.addAll(module.getDepends());
        }
        return visitedModules;
    }

    @NotNull
    public GlobalSearchScope getOdooModuleWithDependenciesScope() {
        return getOdooModuleScope(true, false);
    }

    @NotNull
    public GlobalSearchScope getOdooModuleScope(boolean includeDependencies,
                                                boolean includeExtensions) {
        return PyUtil.getParameterizedCachedValue(getDirectory(), Pair.create(includeDependencies, includeExtensions), param -> {
            List<OdooModule> modules = new LinkedList<>();
            modules.add(this);
            if (includeDependencies) {
                modules.addAll(getFlattenedDependsGraph());
                List<String> systemWideModuleNames = new LinkedList<>(Arrays.asList("web", "base"));
                for (OdooModule module : modules) {
                    systemWideModuleNames.remove(module.getName());
                }
                for (String moduleName : systemWideModuleNames) {
                    OdooModule module = OdooModuleIndex.getOdooModuleByName(moduleName, getDirectory());
                    if (module != null) {
                        modules.add(module);
                    }
                }
            }
            if (includeExtensions) {
                List<OdooModule> availableModules = OdooModuleIndex.getAvailableOdooModules(getDirectory());
                for (OdooModule module : availableModules) {
                    if (module.isDependOn(this)) {
                        modules.add(module);
                    }
                }
            }
            List<VirtualFile> dirs = new LinkedList<>();
            for (OdooModule module : modules) {
                dirs.add(module.getDirectory().getVirtualFile());
            }
            return GlobalSearchScopes.directoriesScope(getProject(), true, dirs.toArray(VirtualFile.EMPTY_ARRAY));
        });
    }

    public GlobalSearchScope getOdooModuleWithExtensionsScope() {
        return getOdooModuleScope(false, true);
    }

    public boolean isDependOn(@Nullable OdooModule module) {
        if (module == null) {
            return false;
        }
        return !this.equals(module) && (getDepends().contains(module) || getFlattenedDependsGraph().contains(module));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooModule that = (OdooModule) o;
        return myDirectory.equals(that.myDirectory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myDirectory);
    }
}
