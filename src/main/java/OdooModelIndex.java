import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.stubs.PyClassNameIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModelIndex extends FileBasedIndexExtension<String, String> {
    public static final @NotNull ID<String, String> NAME = ID.create("odoo.model");
    private static final Gson GSON = new Gson();
    private static final String KEY_moduleName = "moduleName";
    private static final String KEY_className = "className";
    private static final String KEY_isPrimary = "isPrimary";
    private static final String KEY_inherit = "inherit";

    private DataIndexer<String, String, FileContent> myDataIndexer = inputData -> {
        HashMap<String, String> result = new HashMap<>();
        VirtualFile virtualFile = inputData.getFile();
        PsiFile psiFile = PsiManager.getInstance(inputData.getProject()).findFile(virtualFile);
        if (psiFile instanceof PyFile) {
            PyFile pyFile = (PyFile) psiFile;
            pyFile.getTopLevelClasses().forEach(pyClass -> {
                OdooModelInfo info = OdooModelInfo.readFromClass(pyClass);
                if (info != null) {
                    Map<String, Object> value = new HashMap<>();
                    value.put(KEY_moduleName, info.getModuleName());
                    value.put(KEY_className, pyClass.getName());
                    value.put(KEY_isPrimary, info.isPrimary());
                    value.put(KEY_inherit, info.getInherit());
                    result.put(info.getName(), GSON.toJson(value));
                }
            });
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
        return OdooModelInputFilter.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Nullable
    public static OdooModelInfo getModelInfo(@NotNull PyClass pyClass) {
        PsiFile psiFile = pyClass.getContainingFile();
        if (psiFile == null) {
            return null;
        }

        FileBasedIndex index = FileBasedIndex.getInstance();
        GlobalSearchScope scope = GlobalSearchScope.fileScope(psiFile);

        List<String> models = new LinkedList<>();
        index.processAllKeys(NAME, s -> {
            models.add(s);
            return true;
        }, scope, null);

        String className = pyClass.getName();
        List<Map<String, Object>> values = new LinkedList<>();
        models.forEach(s -> {
            index.processValues(NAME, s, psiFile.getVirtualFile(), (file, value) -> {
                Map<String, Object> valueMap = deserializeValue(value);
                String name = (String) valueMap.get(KEY_className);
                if (name.equals(className)) {
                    valueMap.put("model", s);
                    values.add(valueMap);
                    return false;
                }
                return true;
            }, scope);
        });
        if (values.isEmpty()) {
            return null;
        }

        Map<String, Object> value = values.get(0);
        String model = (String) value.get("model");
        String moduleName = (String) value.get(KEY_moduleName);
        boolean isPrimary = (boolean) value.get(KEY_isPrimary);
        @SuppressWarnings("unchecked") List<String> inherit = (List<String>) value.get(KEY_inherit);
        return new OdooModelInfo(model, moduleName, isPrimary, inherit);
    }

    @NotNull
    @SuppressWarnings("unchecked")
    private static Map<String, Object> deserializeValue(@NotNull String value) {
        return GSON.fromJson(value, Map.class);
    }

    @NotNull
    public static List<PyClass> findModelClasses(@NotNull String model, @NotNull PsiDirectory module) {
        List<PyClass> result = new LinkedList<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        Map<VirtualFile, String> classMap = new HashMap<>();
        index.processValues(NAME, model, null, (file, value) -> {
            Map<String, Object> valueMap = deserializeValue(value);
            String className = (String) valueMap.get(KEY_className);
            classMap.put(file, className);
            return true;
        }, GlobalSearchScopesCore.directoryScope(module, true));
        Project project = module.getProject();
        classMap.forEach((virtualFile, s) -> {
            Collection<PyClass> pyClasses = PyClassNameIndex.find(s, project, GlobalSearchScope.fileScope(project, virtualFile));
            result.addAll(pyClasses);
        });
        return result;
    }
}
