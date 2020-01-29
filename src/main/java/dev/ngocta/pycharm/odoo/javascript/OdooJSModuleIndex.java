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
import dev.ngocta.pycharm.odoo.module.OdooModule;
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
                getModuleDefinesInFile((JSFile) psiFile).forEach((moduleName, moduleFunc) -> {
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

    private static Map<String, JSFunctionExpression> getModuleDefinesInFile(@NotNull JSFile file) {
        return CachedValuesManager.getCachedValue(file, () -> {
            Map<String, JSFunctionExpression> result = new HashMap<>();
            for (JSExpressionStatement statement : PsiTreeUtil.getChildrenOfTypeAsList(file, JSExpressionStatement.class)) {
                JSExpression expression = statement.getExpression();
                if (expression instanceof JSCallExpression) {
                    JSExpression method = ((JSCallExpression) expression).getMethodExpression();
                    if (method instanceof JSReferenceExpression && "odoo.define".equals(method.getText())) {
                        JSExpression[] args = ((JSCallExpression) expression).getArguments();
                        if (args.length > 1 && args[0] instanceof JSLiteralExpression) {
                            String moduleName = ((JSLiteralExpression) args[0]).getStringValue();
                            if (args.length == 2 && args[1] instanceof JSFunctionExpression) {
                                result.put(moduleName, (JSFunctionExpression) args[1]);
                            } else if (args.length == 3 && args[2] instanceof JSFunctionExpression) {
                                result.put(moduleName, (JSFunctionExpression) args[2]);
                            }
                        }
                    }
                }
            }
            return CachedValueProvider.Result.create(result, file);
        });
    }

    @NotNull
    public static Collection<String> getAvailableModuleNames(@NotNull PsiElement anchor) {
        OdooModule module = OdooModule.findModule(anchor);
        if (module != null) {
            return getAllModuleNames(module.getSearchScope());
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

    @Nullable
    public static JSFunctionExpression findModuleDefineFunction(@NotNull String moduleName, @NotNull PsiElement anchor) {
        OdooModule module = OdooModule.findModule(anchor);
        if (module != null) {
            Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(NAME, moduleName, module.getSearchScope());
            if (files.isEmpty()) {
                return null;
            }
            PsiFile psiFile = PsiManager.getInstance(anchor.getProject()).findFile(files.iterator().next());
            if (psiFile instanceof JSFile) {
                Map<String, JSFunctionExpression> map = getModuleDefinesInFile((JSFile) psiFile);
                for (Map.Entry<String, JSFunctionExpression> entry : map.entrySet()) {
                    if (moduleName.equals(entry.getKey())) {
                        return entry.getValue();
                    }
                }
            }
        }
        return null;
    }
}
