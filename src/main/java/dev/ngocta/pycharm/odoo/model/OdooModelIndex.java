package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OdooModelIndex extends ScalarIndexExtension<String> {
    public static final @NotNull ID<String, Void> NAME = ID.create("odoo.model");

    private DataIndexer<String, Void, FileContent> myDataIndexer = inputData -> {
        Map<String, Void> result = new HashMap<>();
        VirtualFile virtualFile = inputData.getFile();
        PsiFile psiFile = PsiManager.getInstance(inputData.getProject()).findFile(virtualFile);
        if (OdooUtils.isOdooModelFile(psiFile)) {
            PyFile pyFile = (PyFile) psiFile;
            pyFile.getTopLevelClasses().forEach(pyClass -> {
                OdooModelInfo info = OdooModelInfo.readFromClass(pyClass);
                if (info != null) {
                    result.put(info.getName(), null);
                }
            });
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
        return 2;
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

    @NotNull
    public static List<PyClass> findModelClasses(@NotNull String model, @NotNull Project project, GlobalSearchScope scope) {
        List<PyClass> result = new LinkedList<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        Set<VirtualFile> files = new HashSet<>();
        index.getFilesWithKey(NAME, Collections.singleton(model), files::add, scope);
        PsiManager psiManager = PsiManager.getInstance(project);
        files.forEach(file -> {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof PyFile) {
                ConcurrentMap<String, List<PyClass>> cache = CachedValuesManager.getCachedValue(psiFile, () -> {
                    return CachedValueProvider.Result.create(new ConcurrentHashMap<>(), psiFile);
                });
                List<PyClass> cachedClasses = cache.get(model);
                if (cachedClasses != null) {
                    result.addAll(cachedClasses);
                }
                if (cachedClasses == null) {
                    List<PyClass> classes = new LinkedList<>();
                    ((PyFile) psiFile).getTopLevelClasses().forEach(cls -> {
                        OdooModelInfo info = OdooModelInfo.readFromClass(cls);
                        if (info != null && info.getName().equals(model)) {
                            classes.add(cls);
                        }
                    });
                    cache.put(model, classes);
                    result.addAll(classes);
                }
            }
        });
        return result;
    }

    @NotNull
    public static List<PyClass> findModelClasses(@NotNull String model, @NotNull PsiDirectory module) {
        return findModelClasses(model, module.getProject(), GlobalSearchScopesCore.directoryScope(module, true));
    }
}
