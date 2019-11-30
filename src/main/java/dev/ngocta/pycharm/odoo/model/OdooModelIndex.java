package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
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
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.OdooUtils;
import dev.ngocta.pycharm.odoo.module.OdooModuleIndex;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OdooModelIndex extends ScalarIndexExtension<String> {
    public static final @NotNull ID<String, Void> NAME = ID.create("odoo.model");

    private final DataIndexer<String, Void, FileContent> myDataIndexer = inputData -> {
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

    public static List<PyClass> findModelClasses(@NotNull String model, @NotNull PsiDirectory module, boolean includeDependModules) {
        Project project = module.getProject();
        if (includeDependModules) {
            List<PyClass> result = new LinkedList<>();
            OdooModuleIndex.getFlattenedDependsGraph(module).forEach(mod -> {
                result.addAll(findModelClasses(model, mod, false));
            });
            return result;
        } else {
            return PyUtil.getParameterizedCachedValue(module, model, modelArg -> {
                return findModelClasses(modelArg, project, GlobalSearchScopesCore.directoryScope(module, true));
            });
        }
    }

    @NotNull
    public static List<PyClass> findModelClasses(@NotNull String model, @NotNull PsiElement anchor, boolean includeDependModules) {
        PsiDirectory module = OdooUtils.getOdooModuleDir(anchor);
        if (module != null) {
            return findModelClasses(model, module, includeDependModules);
        }
        return Collections.emptyList();
    }

    @NotNull
    public static Collection<String> getAllModels(@NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        return new HashSet<>(index.getAllKeys(NAME, project));
    }
}
