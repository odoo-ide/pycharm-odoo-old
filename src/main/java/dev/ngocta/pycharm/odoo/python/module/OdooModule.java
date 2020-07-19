package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public GlobalSearchScope getSearchScope() {
        return getSearchScope(true);
    }

    @NotNull
    public GlobalSearchScope getSearchScope(boolean includeDepends) {
        return PyUtil.getParameterizedCachedValue(getDirectory(), includeDepends, param -> {
            if (includeDepends) {
                List<OdooModule> modules = getFlattenedDependsGraph();
                VirtualFile[] dirs = modules.stream()
                        .map(OdooModule::getDirectory)
                        .map(PsiDirectory::getVirtualFile)
                        .collect(Collectors.toList())
                        .toArray(VirtualFile.EMPTY_ARRAY);
                return GlobalSearchScopes.directoriesScope(getProject(), true, dirs);
            }
            return GlobalSearchScopes.directoryScope(getDirectory(), true);
        });
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
