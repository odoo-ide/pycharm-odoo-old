package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.JavaScriptFileType;
import com.intellij.lang.javascript.psi.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooJSModuleIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> NAME = ID.create("odoo.js.module");

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
            PsiFile psiFile = PsiManager.getInstance(inputData.getProject()).findFile(virtualFile);
            if (OdooJSUtils.isOdooJSFile(psiFile)) {
                getModuleDefineCallsInFile((JSFile) psiFile).forEach((moduleName, moduleDefineCall) -> {
                    result.put(moduleName, null);
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

    @Override
    public int getVersion() {
        return 0;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(JavaScriptFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private static Map<String, JSCallExpression> getModuleDefineCallsInFile(@NotNull JSFile file) {
        return CachedValuesManager.getCachedValue(file, () -> {
            Map<String, JSCallExpression> result = new HashMap<>();
            for (JSExpressionStatement statement : PsiTreeUtil.getChildrenOfTypeAsList(file, JSExpressionStatement.class)) {
                JSExpression expression = statement.getExpression();
                if (expression instanceof JSCallExpression) {
                    JSExpression method = ((JSCallExpression) expression).getMethodExpression();
                    if (method instanceof JSReferenceExpression && "odoo.define".equals(method.getText())) {
                        JSExpression[] args = ((JSCallExpression) expression).getArguments();
                        if (args.length > 1 && args[0] instanceof JSLiteralExpression) {
                            String moduleName = ((JSLiteralExpression) args[0]).getStringValue();
                            result.put(moduleName, (JSCallExpression) expression);
                        }
                    }
                }
            }
            return CachedValueProvider.Result.create(result, file);
        });
    }

    @NotNull
    public static Collection<String> getAvailableModuleNames(@NotNull PsiElement anchor) {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(anchor);
        if (module != null) {
            return getAllModuleNames(module.getOdooModuleWithDependenciesScope());
        }
        return getAllModuleNames(anchor.getProject());
    }

    public static Collection<String> getAllModuleNames(@NotNull Project project) {
        return new HashSet<>(FileBasedIndex.getInstance().getAllKeys(NAME, project));
    }

    @NotNull
    public static Collection<String> getAllModuleNames(@NotNull GlobalSearchScope scope) {
        Project project = scope.getProject();
        if (project == null) {
            return Collections.emptySet();
        }
        Set<String> result = new HashSet<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<String> keys = index.getAllKeys(NAME, project);
        keys.forEach(name -> {
            index.processValues(NAME, name, null, (file, value) -> {
                result.add(name);
                return true;
            }, scope);
        });
        return result;
    }

    @NotNull
    public static Collection<OdooJSModule> findModules(@NotNull String moduleName,
                                                       @NotNull GlobalSearchScope scope,
                                                       @NotNull Project project) {
        List<OdooJSModule> modules = new LinkedList<>();
        Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(NAME, moduleName, scope);
        if (files.isEmpty()) {
            return modules;
        }
        for (VirtualFile file : files) {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile instanceof JSFile) {
                Map<String, JSCallExpression> map = getModuleDefineCallsInFile((JSFile) psiFile);
                for (Map.Entry<String, JSCallExpression> entry : map.entrySet()) {
                    if (moduleName.equals(entry.getKey())) {
                        modules.add(new OdooJSModule(moduleName, entry.getValue()));
                    }
                }
            }
        }
        return modules;
    }

    @Nullable
    public static OdooJSModule findModule(@NotNull String moduleName,
                                          @NotNull PsiElement anchor) {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(anchor);
        if (module != null) {
            Collection<OdooJSModule> modules = findModules(moduleName, module.getOdooModuleWithDependenciesScope(), anchor.getProject());
            return modules.iterator().next();
        }
        return null;
    }
}
