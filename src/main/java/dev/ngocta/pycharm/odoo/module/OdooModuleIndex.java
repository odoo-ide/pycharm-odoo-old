package dev.ngocta.pycharm.odoo.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.PythonFileType;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModuleIndex extends ScalarIndexExtension<String> {
    public static final @NotNull ID<String, Void> NAME = ID.create("odoo.module");

    @NotNull
    private DataIndexer<String, Void, FileContent> myDataIndexer = inputData -> {
        Map<String, Void> result = new HashMap<>();
        VirtualFile file = inputData.getFile();
        if (OdooNames.MANIFEST.equals(file.getName())) {
            VirtualFile dir = file.getParent();
            if (dir != null) {
                result.put(dir.getName(), null);
            }
        }
        return result;
    };

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return myDataIndexer;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(PythonFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Nullable
    private static PsiFile getModuleManifest(@NotNull String moduleName, @NotNull Project project) {
        FileBasedIndex fileIndex = FileBasedIndex.getInstance();
        Collection<VirtualFile> files = fileIndex.getContainingFiles(NAME, moduleName, GlobalSearchScope.allScope(project));
        for (VirtualFile file : files) {
            return PsiManager.getInstance(project).findFile(file);
        }
        return null;
    }

    @Nullable
    public static PsiDirectory getModule(@NotNull String moduleName, @NotNull Project project) {
        PsiFile manifest = getModuleManifest(moduleName, project);
        if (manifest != null) {
            return manifest.getContainingDirectory();
        }
        return null;
    }

    @NotNull
    public static Collection<PsiDirectory> getAllModules(@NotNull Project project) {
        FileBasedIndex fileIndex = FileBasedIndex.getInstance();
        ArrayList<PsiDirectory> dirs = new ArrayList<>();
        Collection<String> names = fileIndex.getAllKeys(NAME, project);
        for (String name : names) {
            PsiDirectory dir = getModule(name, project);
            if (dir != null) {
                dirs.add(dir);
            }
        }
        return dirs;
    }

    @NotNull
    private static List<PsiDirectory> getDepends(@NotNull PsiFile manifest) {
        List<PsiDirectory> result = new LinkedList<>();
        Project project = manifest.getProject();
        OdooModuleInfo info = OdooModuleInfo.readFromManifest(manifest);
        if (info != null) {
            info.getDepends().forEach(s -> {
                PsiDirectory module = getModule(s, project);
                if (module != null) {
                    result.add(module);
                }
            });
        }
        return result;
    }

    public static List<PsiDirectory> getDepends(@NotNull PsiDirectory module) {
        PsiFile manifest = module.findFile(OdooNames.MANIFEST);
        if (manifest != null) {
            return getDepends(manifest);
        }
        return Collections.emptyList();
    }

    public static List<PsiDirectory> getFlattenedDependsGraph(@NotNull PsiDirectory module) {
        List<PsiDirectory> visitedNodes = new LinkedList<>();
        List<PsiDirectory> queue = new LinkedList<>();
        queue.add(module);
        PsiDirectory node;
        while (!queue.isEmpty()) {
            node = queue.remove(0);
            visitedNodes.remove(node);
            visitedNodes.add(node);
            queue.addAll(getDepends(node));
        }
        return visitedNodes;
    }
}
