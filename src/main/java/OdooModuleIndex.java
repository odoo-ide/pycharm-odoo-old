import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OdooModuleIndex extends ScalarIndexExtension<String> {
    public static final @NotNull ID<String, Void> NAME = ID.create("odoo.module");

    private @NotNull DataIndexer<String, Void, FileContent> myDataIndexer = inputData -> {
        VirtualFile moduleDir = inputData.getFile().getParent();
        return Collections.singletonMap(moduleDir.getName(), null);
    };

    @Override
    public @NotNull ID<String, Void> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, Void, FileContent> getIndexer() {
        return myDataIndexer;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return OdooManifestInputFilter.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return false;
    }

    public static PsiDirectory getModuleByName(String name, Project project) {
        FileBasedIndex fileIndex = FileBasedIndex.getInstance();
        Collection<VirtualFile> files = fileIndex.getContainingFiles(NAME, name, GlobalSearchScope.allScope(project));
        for (VirtualFile file : files) {
            VirtualFile dir = file.getParent();
            return PsiManager.getInstance(project).findDirectory(dir);
        }
        return null;
    }

    public static Collection<PsiDirectory> getAllModule(Project project) {
        FileBasedIndex fileIndex = FileBasedIndex.getInstance();
        ArrayList<PsiDirectory> dirs = new ArrayList<>();
        Collection<String> names = fileIndex.getAllKeys(NAME, project);
        for (String name : names) {
            PsiDirectory dir = getModuleByName(name, project);
            if (dir != null) {
                dirs.add(dir);
            }
        }
        return dirs;
    }
}
