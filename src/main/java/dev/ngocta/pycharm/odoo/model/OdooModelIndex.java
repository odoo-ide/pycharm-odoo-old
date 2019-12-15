package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopes;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.util.indexing.*;
import com.intellij.util.io.BooleanDataDescriptor;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import dev.ngocta.pycharm.odoo.module.OdooModuleIndex;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OdooModelIndex extends FileBasedIndexExtension<String, Boolean> {
    public static final @NotNull ID<String, Boolean> NAME = ID.create("odoo.model");

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
            PsiFile psiFile = PsiManager.getInstance(inputData.getProject()).findFile(virtualFile);
            if (OdooUtils.isOdooModelFile(psiFile)) {
                PyFile pyFile = (PyFile) psiFile;
                pyFile.getTopLevelClasses().forEach(pyClass -> {
                    OdooModelInfo info = OdooModelInfo.getInfo(pyClass);
                    if (info != null) {
                        result.put(info.getName(), info.isOriginal());
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
        return 3;
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
                ((PyFile) psiFile).getTopLevelClasses().forEach(cls -> {
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
    public static List<PyClass> findModelClasses(@NotNull String model, @NotNull PsiDirectory module, boolean includeDependModules) {
        Project project = module.getProject();
        if (includeDependModules) {
            List<PyClass> result = new LinkedList<>();
            OdooModuleIndex.getFlattenedDependsGraph(module).forEach(mod -> {
                result.addAll(findModelClasses(model, mod, false));
            });
            return result;
        } else {
            return findModelClasses(model, project, GlobalSearchScopesCore.directoryScope(module, true));
        }
    }

    @NotNull
    public static List<PyClass> findModelClasses(@NotNull String model, @NotNull PsiElement anchor, boolean includeDependModules) {
        PsiDirectory module = OdooUtils.getOdooModule(anchor);
        if (module == null) {
            module = OdooModuleIndex.getModule(OdooNames.MODULE_BASE, anchor.getProject());
        }
        if (module != null) {
            return findModelClasses(model, module, includeDependModules);
        }
        return Collections.emptyList();
    }

    @NotNull
    public static Set<String> getAvailableModels(@NotNull PsiElement anchor) {
        Set<String> result = new HashSet<>();
        Project project = anchor.getProject();
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<String> models = index.getAllKeys(NAME, project);
        List<PsiDirectory> modules = OdooModuleIndex.getFlattenedDependsGraph(anchor);
        modules.forEach(module -> {
            GlobalSearchScope scope = GlobalSearchScopes.directoryScope(module, true);
            models.forEach(model -> index.processValues(NAME, model, null, (file, value) -> {
                if (value) {
                    result.add(model);
                }
                return true;
            }, scope));
        });
        return result;
    }
}
