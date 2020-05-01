package dev.ngocta.pycharm.odoo.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.BooleanDataDescriptor;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElementVisitor;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.data.OdooRecordCache;
import dev.ngocta.pycharm.odoo.data.OdooRecordImpl;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModelIndex extends FileBasedIndexExtension<String, Boolean> {
    public static final @NotNull ID<String, Boolean> NAME = ID.create("odoo.model");
    private static final OdooRecordCache IR_MODEL_RECORD_CACHE = new OdooRecordCache();

    @NotNull
    @Override
    public ID<String, Boolean> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Boolean, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Boolean> result = new HashMap<>();
            VirtualFile virtualFile = inputData.getFile();
            Project project = inputData.getProject();
            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if (OdooModelUtils.isOdooModelFile(psiFile)) {
                psiFile.acceptChildren(new PyElementVisitor() {
                    @Override
                    public void visitPyClass(PyClass node) {
                        super.visitPyClass(node);
                        OdooModelInfo info = OdooModelInfo.getInfo(node);
                        if (info != null) {
                            result.putIfAbsent(info.getName(), info.isOriginal());
                            updateIrModelRecordCache(info.getName(), virtualFile, project);
                        }
                    }
                });
            }
            return result;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public DataExternalizer<Boolean> getValueExternalizer() {
        return BooleanDataDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 4;
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
    private static List<PyClass> findModelClasses(@NotNull String model,
                                                  @NotNull Project project,
                                                  @NotNull GlobalSearchScope scope) {
        List<PyClass> result = new LinkedList<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        Set<VirtualFile> files = new HashSet<>();
        index.getFilesWithKey(NAME, Collections.singleton(model), files::add, scope);
        PsiManager psiManager = PsiManager.getInstance(project);
        files.forEach(file -> {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof PyFile) {
                List<PyClass> classes = ((PyFile) psiFile).getTopLevelClasses();
                classes = Lists.reverse(classes);
                classes.forEach(cls -> {
                    OdooModelInfo info = OdooModelInfo.getInfo(cls);
                    if (info != null && info.getName().equals(model)) {
                        result.add(cls);
                    }
                });
            }
        });
        return result;
    }

    @NotNull
    private static List<PyClass> findModelClasses(@NotNull String model,
                                                  @NotNull OdooModule module) {
        Project project = module.getProject();
        return PyUtil.getParameterizedCachedValue(module.getDirectory(), model, param -> {
            List<PyClass> result = new LinkedList<>();
            module.getFlattenedDependsGraph().forEach(mod -> {
                result.addAll(findModelClasses(model, project, mod.getSearchScope(false)));
            });
            return ImmutableList.copyOf(result);
        });
    }

    @NotNull
    public static List<PyClass> findModelClasses(@NotNull String model,
                                                 @NotNull PsiElement anchor) {
        Project project = anchor.getProject();
        OdooModule odooModule = OdooModule.findModule(anchor);
        if (odooModule != null) {
            return findModelClasses(model, odooModule);
        }
        return findModelClasses(model, project, OdooUtils.getProjectScope(anchor));
    }

    @NotNull
    public static Collection<String> getAvailableModels(@NotNull PsiElement anchor) {
        OdooModule module = OdooModule.findModule(anchor);
        if (module != null) {
            Collection<String> models = getAllModels(anchor.getProject());
            Set<String> result = new HashSet<>();
            module.getFlattenedDependsGraph().forEach(mod -> {
                result.addAll(filterModels(models, mod.getSearchScope(false)));
            });
            return result;
        }
        return Collections.emptyList();
    }

    @NotNull
    public static Collection<String> getAllModels(@NotNull Project project) {
        return new HashSet<>(FileBasedIndex.getInstance().getAllKeys(NAME, project));
    }

    @NotNull
    private static Collection<String> filterModels(@NotNull Collection<String> models,
                                                   @NotNull GlobalSearchScope scope) {
        Set<String> result = new HashSet<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        models.forEach(model -> index.processValues(NAME, model, null, (file, value) -> {
            if (value) {
                result.add(model);
            }
            return true;
        }, scope));
        return result;
    }

    public static boolean processIrModelRecords(@NotNull Project project,
                                                @NotNull GlobalSearchScope scope,
                                                @NotNull Processor<OdooRecord> processor) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<String> models = index.getAllKeys(NAME, project);
        GlobalSearchScope everythingScope = new EverythingGlobalScope(project);
        for (String model : models) {
            if (!IR_MODEL_RECORD_CACHE.processRecords(model, processor, scope)) {
                if (!index.processValues(NAME, model, null, (file, value) -> {
                    OdooRecord record = updateIrModelRecordCache(model, file, project);
                    if (record != null) {
                        if (scope.contains(file)) {
                            return processor.process(record);
                        }
                    }
                    return true;
                }, everythingScope)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Nullable
    private static OdooRecord updateIrModelRecordCache(@NotNull String model,
                                                       @NotNull VirtualFile file,
                                                       @NotNull Project project) {
        OdooModule module = OdooModule.findModule(file, project);
        if (module != null) {
            String name = "model_" + model.replace(".", "_");
            OdooRecord record = new OdooRecordImpl(name, OdooNames.IR_MODEL, module.getName(), null, file);
            IR_MODEL_RECORD_CACHE.add(model, record);
            return record;
        }
        return null;
    }
}
