package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
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
    public List<OdooModule> getDependencies() {
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
    public List<OdooModule> getRecursiveDependencies() {
        return PyUtil.getParameterizedCachedValue(getDirectory(), null, param -> {
            List<OdooModule> result = new LinkedList<>();
            List<DefaultMutableTreeNode> nodesToVisit = new LinkedList<>();
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(this);
            nodesToVisit.add(root);
            while (!nodesToVisit.isEmpty()) {
                DefaultMutableTreeNode node = nodesToVisit.remove(0);
                List<Object> dependPath = Arrays.asList(node.getUserObjectPath());
                OdooModule module = (OdooModule) node.getUserObject();
                result.remove(module);
                result.add(module);
                for (OdooModule depend : module.getDependencies()) {
                    if (dependPath.contains(depend)) {
                        continue;
                    }
                    if (nodesToVisit.stream().anyMatch(n -> depend.equals(n.getUserObject()))) {
                        continue;
                    }
                    DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(depend);
                    node.add(childNode);
                    nodesToVisit.add(childNode);
                }
            }
            return result;
        });
    }

    @NotNull
    public List<OdooModule> getExtensions() {
        return PyUtil.getParameterizedCachedValue(getDirectory(), null, param -> {
            return OdooModuleDependIndex.getDependingOdooModules(getName(), getDirectory());
        });
    }

    @NotNull
    public List<OdooModule> getRecursiveExtensions() {
        return PyUtil.getParameterizedCachedValue(getDirectory(), null, param -> {
            List<OdooModule> result = new LinkedList<>();
            collectRecursiveExtensions(result);
            return result;
        });
    }

    private void collectRecursiveExtensions(List<OdooModule> result) {
        for (OdooModule extension : getExtensions()) {
            if (!result.contains(extension)) {
                result.add(extension);
            }
            extension.collectRecursiveExtensions(result);
        }
    }

    @NotNull
    public GlobalSearchScope getOdooModuleWithDependenciesScope() {
        return getOdooModuleScope(true, false);
    }

    @NotNull
    public GlobalSearchScope getOdooModuleWithExtensionsScope() {
        return getOdooModuleScope(false, true);
    }

    @NotNull
    public GlobalSearchScope getOdooModuleWithDependenciesAndExtensionsScope() {
        return getOdooModuleScope(true, true);
    }

    @NotNull
    public GlobalSearchScope getOdooModuleScope() {
        return getOdooModuleScope(false, false);
    }

    @NotNull
    public GlobalSearchScope getOdooModuleScope(boolean includeDependencies,
                                                boolean includeExtensions) {
        return PyUtil.getParameterizedCachedValue(getDirectory(), Pair.create(includeDependencies, includeExtensions), param -> {
            List<OdooModule> modules = new LinkedList<>();
            modules.add(this);
            if (includeDependencies) {
                modules.addAll(getRecursiveDependencies());
                for (OdooModule module : OdooModuleUtils.getSystemWideOdooModules(getDirectory())) {
                    if (!modules.contains(module)) {
                        modules.add(module);
                    }
                }
            }
            if (includeExtensions) {
                modules.addAll(getRecursiveExtensions());
            }
            List<VirtualFile> files = new LinkedList<>();
            for (OdooModule module : modules) {
                VfsUtilCore.processFilesRecursively(module.getDirectory().getVirtualFile(), files::add);
            }
            return GlobalSearchScope.filesWithLibrariesScope(getProject(), files);
        });
    }

    public boolean isDependOn(@Nullable OdooModule module) {
        if (module == null) {
            return false;
        }
        return !this.equals(module) && (getDependencies().contains(module) || getRecursiveDependencies().contains(module));
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

    @Override
    public String toString() {
        return "OdooModule:" + myDirectory.getVirtualFile().getPresentableUrl();
    }
}
