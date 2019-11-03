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
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModuleIndex extends FileBasedIndexExtension<String, String> {
    public static final @NotNull ID<String, String> NAME = ID.create("odoo.module");

    @NotNull
    private DataIndexer<String, String, FileContent> myDataIndexer = inputData -> {
        Map<String, String> result = new HashMap<>();
        OdooModuleInfo info = OdooModuleInfo.readFromManifest(inputData);
        if (info != null) {
            VirtualFile parent = inputData.getFile().getParent();
            List<String> depends = info.getDepends();
            result.put(parent.getName(), String.join(",", depends));
        }
        return result;
    };

    @NotNull
    @Override
    public ID<String, String> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return myDataIndexer;
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return OdooManifestInputFilter.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Nullable
    public static PsiDirectory getModuleByName(@NotNull String name, @NotNull Project project) {
        FileBasedIndex fileIndex = FileBasedIndex.getInstance();
        Collection<VirtualFile> files = fileIndex.getContainingFiles(NAME, name, GlobalSearchScope.allScope(project));
        for (VirtualFile file : files) {
            VirtualFile dir = file.getParent();
            return PsiManager.getInstance(project).findDirectory(dir);
        }
        return null;
    }

    @NotNull
    public static Collection<PsiDirectory> getAllModules(@NotNull Project project) {
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

    @NotNull
    public static List<String> getDepends(@NotNull String moduleName, @NotNull Project project) {
        List<String> depends = new LinkedList<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        index.processValues(OdooModuleIndex.NAME, moduleName, null, (file, value) -> {
            depends.addAll(Arrays.asList(value.split(",")));
            return false;
        }, GlobalSearchScope.allScope(project));
        return depends;
    }

    @NotNull
    static List<PsiDirectory> getDependModules(@NotNull String moduleName, @NotNull Project project) {
        List<PsiDirectory> result = new LinkedList<>();
        getDepends(moduleName, project).forEach(s -> {
            PsiDirectory dir = getModuleByName(s, project);
            if (dir != null) {
                result.add(dir);
            }
        });
        return result;
    }

    @NotNull
    public static List<String> getDependsRecursive(@NotNull String moduleName, @NotNull Project project) {
        List<String> depends = new LinkedList<>();
        resolveDependsOfModule(moduleName, project, depends);
        return depends;
    }

    private static void resolveDependsOfModule(@NotNull String moduleName, @NotNull Project project, @NotNull List<String> depends) {
        if (!depends.contains(moduleName)) {
            depends.add(moduleName);
            getDepends(moduleName, project).forEach(s -> {
                resolveDependsOfModule(moduleName, project, depends);
            });
        }
    }
}
