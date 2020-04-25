package dev.ngocta.pycharm.odoo.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import com.jetbrains.python.PyNames;
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

    @Nullable
    private static PsiFile findManifestFileInDirectory(PsiDirectory dir) {
        return dir.findFile(OdooNames.MANIFEST_FILE_NAME);
    }

    @Nullable
    private static PsiDirectory findModuleDirectory(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        if (element instanceof PsiDirectory) {
            PsiDirectory dir = (PsiDirectory) element;
            if (findManifestFileInDirectory(dir) != null && dir.findFile(PyNames.INIT_DOT_PY) != null) {
                return dir;
            }
        }
        element = element.getOriginalElement();
        PsiElement parent = element.getParent();
        if (parent == null) {
            parent = element.getContext();
        }
        return findModuleDirectory(parent);
    }

    @Nullable
    public static OdooModule findModule(@NotNull PsiElement element) {
        PsiDirectory dir = findModuleDirectory(element);
        if (dir != null) {
            return new OdooModule(dir);
        }
        return null;
    }

    @Nullable
    public static OdooModule findModule(@NotNull VirtualFile file, @NotNull Project project) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        return psiFile != null ? findModule(psiFile) : null;
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
    public PsiFile getManifestFile() {
        return findManifestFileInDirectory(myDirectory);
    }

    @Nullable
    public OdooManifestInfo getManifestInfo() {
        PsiFile manifest = getManifestFile();
        if (manifest != null) {
            return OdooManifestInfo.getInfo(manifest);
        }
        return null;
    }

    @NotNull
    public List<OdooModule> getDepends() {
        OdooManifestInfo info = getManifestInfo();
        if (info == null) {
            return Collections.emptyList();
        }
        List<OdooModule> result = new LinkedList<>();
        for (String depend : info.getDepends()) {
            OdooModule module = OdooModuleIndex.getModule(depend, getDirectory());
            if (module != null) {
                result.add(module);
            }
        }
        return result;
    }

    @NotNull
    public List<OdooModule> getFlattenedDependsGraph() {
        List<OdooModule> visitedNodes = new LinkedList<>();
        List<OdooModule> queue = new LinkedList<>();
        queue.add(this);
        OdooModule node;
        while (!queue.isEmpty()) {
            node = queue.remove(0);
            visitedNodes.remove(node);
            visitedNodes.add(node);
            queue.addAll(node.getDepends());
        }
        return visitedNodes;
    }

    @NotNull
    public GlobalSearchScope getSearchScope() {
        return getSearchScope(true);
    }

    @NotNull
    public GlobalSearchScope getSearchScope(boolean includeDepends) {
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
