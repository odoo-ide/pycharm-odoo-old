package dev.ngocta.pycharm.odoo.python.model;

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
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElementVisitor;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.data.OdooRecordCache;
import dev.ngocta.pycharm.odoo.data.OdooRecordImpl;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModelIndex extends FileBasedIndexExtension<String, Void> {
    public static final @NotNull ID<String, Void> NAME = ID.create("odoo.model");
    private static final OdooRecordCache IR_MODEL_RECORD_CACHE = new OdooRecordCache();

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> result = new HashMap<>();
            VirtualFile virtualFile = inputData.getFile();
            Project project = inputData.getProject();
            inputData.getPsiFile().acceptChildren(new PyElementVisitor() {
                @Override
                public void visitPyClass(PyClass cls) {
                    super.visitPyClass(cls);
                    OdooModelInfo info = OdooModelInfo.getInfo(cls);
                    if (info != null) {
                        result.putIfAbsent(info.getName(), null);
                        updateIrModelRecordCache(info.getName(), virtualFile, project);
                    }
                }
            });
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
    public DataExternalizer<Void> getValueExternalizer() {
        return VoidDataExternalizer.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 8;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new OdooModelInputFilter();
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @NotNull
    private static List<PyClass> getOdooModelClassesByNameInFile(@NotNull String model,
                                                                 @NotNull PyFile file) {
        List<PyClass> classes = new LinkedList<>();
        file.acceptChildren(new PyElementVisitor() {
            @Override
            public void visitPyClass(PyClass cls) {
                super.visitPyClass(cls);
                OdooModelInfo info = OdooModelInfo.getInfo(cls);
                if (info != null && info.getName().equals(model)) {
                    classes.add(cls);
                }
            }
        });
        return Lists.reverse(classes);
    }

    @NotNull
    public static List<PyClass> getOdooModelClassesByName(@NotNull String model,
                                                          @NotNull Project project,
                                                          @NotNull GlobalSearchScope scope) {
        List<PyClass> result = new LinkedList<>();
        Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(NAME, model, scope);
        PsiManager psiManager = PsiManager.getInstance(project);
        files.forEach(file -> {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof PyFile) {
                List<PyClass> classes = getOdooModelClassesByNameInFile(model, (PyFile) psiFile);
                result.addAll(classes);
            }
        });
        return result;
    }

    @NotNull
    private static List<PyClass> getAvailableOdooModelClassesByName(@NotNull String model,
                                                                    @NotNull OdooModule module) {
        Project project = module.getProject();
        return PyUtil.getParameterizedCachedValue(module.getDirectory(), model, param -> {
            List<PyClass> classes = getOdooModelClassesByName(model, project, module.getOdooModuleWithDependenciesScope());
            List<PyClass> sortedClasses = OdooModuleUtils.sortElementByOdooModuleDependOrder(classes);
            return ImmutableList.copyOf(sortedClasses);
        });
    }

    @NotNull
    public static List<PyClass> getAvailableOdooModelClassesByName(@NotNull String model,
                                                                   @NotNull PsiElement anchor) {
        Project project = anchor.getProject();
        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(anchor);
        if (odooModule != null) {
            return getAvailableOdooModelClassesByName(model, odooModule);
        }
        return getOdooModelClassesByName(model, project, OdooUtils.getProjectModuleWithDependenciesScope(anchor));
    }

    @NotNull
    public static Collection<String> getAvailableOdooModels(@NotNull PsiElement anchor) {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(anchor);
        if (module != null) {
            Collection<String> models = getAllOdooModels(anchor.getProject());
            models = filterOdooModels(models, module.getOdooModuleWithDependenciesScope());
            return models;
        }
        return Collections.emptyList();
    }

    @NotNull
    public static Collection<String> getAllOdooModels(@NotNull Project project) {
        return FileBasedIndex.getInstance().getAllKeys(NAME, project);
    }

    @NotNull
    private static Collection<String> filterOdooModels(@NotNull Collection<String> models,
                                                       @NotNull GlobalSearchScope scope) {
        Collection<String> result = new LinkedList<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        models.forEach(model -> index.processValues(NAME, model, null, (file, value) -> {
            result.add(model);
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
        OdooModule module = OdooModuleUtils.getContainingOdooModule(file, project);
        if (module != null) {
            String name = OdooModelUtils.getIrModelRecordName(model);
            OdooRecord record = new OdooRecordImpl(name, OdooNames.IR_MODEL, module.getName(), null, file);
            IR_MODEL_RECORD_CACHE.add(model, record);
            return record;
        }
        return null;
    }
}
