package dev.ngocta.pycharm.odoo.python.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElementVisitor;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OdooModelIndex extends FileBasedIndexExtension<String, Void> {
    public static final @NotNull ID<String, Void> NAME = ID.create("odoo.model");

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
            inputData.getPsiFile().acceptChildren(new PyElementVisitor() {
                @Override
                public void visitPyClass(@NotNull PyClass cls) {
                    super.visitPyClass(cls);
                    OdooModelInfo info = OdooModelInfo.getInfo(cls);
                    if (info != null) {
                        result.putIfAbsent(info.getName(), null);
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
            public void visitPyClass(@NotNull PyClass cls) {
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
    public static List<PyClass> getAvailableOdooModelClassesByName(@NotNull String model,
                                                                   @NotNull PsiElement anchor) {
        PsiFile file = anchor.getContainingFile();
        if (file == null) {
            return Collections.emptyList();
        }
        return PyUtil.getParameterizedCachedValue(file, model, param -> {
            GlobalSearchScope scope = OdooModuleUtils.getOdooModuleWithDependenciesOrSystemWideModulesScope(file);
            List<PyClass> classes = getOdooModelClassesByName(model, anchor.getProject(), scope);
            List<PyClass> sortedClasses = OdooModuleUtils.sortElementByOdooModuleDependOrder(classes);
            return ImmutableList.copyOf(sortedClasses);
        });
    }

    @NotNull
    public static Collection<String> getAvailableOdooModels(@NotNull PsiElement anchor) {
        GlobalSearchScope scope = OdooModuleUtils.getOdooModuleWithDependenciesOrSystemWideModulesScope(anchor);
        return getAvailableOdooModels(anchor.getProject(), scope);
    }

    @NotNull
    public static Collection<String> getAvailableOdooModels(@NotNull Project project,
                                                            @NotNull GlobalSearchScope scope) {
        Collection<String> models = getAllOdooModels(project);
        models = filterOdooModels(models, scope);
        return models;
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
}
