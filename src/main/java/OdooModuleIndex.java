import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

public class OdooModuleIndex extends FileBasedIndexExtension<String, OdooModuleInfo> {
    public static final @NotNull ID<String, OdooModuleInfo> NAME = ID.create("odoo.module");

    @Override
    public @NotNull ID<String, OdooModuleInfo> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, OdooModuleInfo, FileContent> getIndexer() {
        return OdooModuleDataIndexer.INSTANCE;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<OdooModuleInfo> getValueExternalizer() {
        return OdooModuleDataExternalizer.INSTANCE;
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
        return true;
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
