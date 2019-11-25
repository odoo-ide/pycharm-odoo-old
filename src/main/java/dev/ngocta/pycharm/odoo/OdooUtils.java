package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.HashMap;
import java.util.Map;

public class OdooUtils {
    @Nullable
    public static VirtualFile getOdooModuleDir(@NotNull VirtualFile file) {
        VirtualFile cur = file;
        while (cur != null) {
            if (cur.findChild(OdooNames.__MANIFEST__DOT_PY) != null) {
                return cur;
            }
            cur = cur.getParent();
        }
        return null;
    }

    @Nullable
    public static PsiDirectory getOdooModuleDir(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file != null) {
            VirtualFile virtualFile = getOdooModuleDir(file.getVirtualFile());
            if (virtualFile != null) {
                return PsiManager.getInstance(element.getProject()).findDirectory(virtualFile);
            }
        }
        return null;
    }

    public static boolean isOdooModelFile(@Nullable PsiFile file) {
        return file instanceof PyFile && getOdooModuleDir(file.getVirtualFile()) != null;
    }

    @Nullable
    public static PyClass createClassByQName(@NotNull String name, @NotNull PsiElement anchor) {
        Project project = anchor.getProject();
        Map<String, PyClass> cache = CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            return CachedValueProvider.Result.create(new HashMap<>(), ModificationTracker.NEVER_CHANGED);
        });
        PyClass cls = cache.get(name);
        if (cls == null) {
            PyPsiFacade psiFacade = PyPsiFacade.getInstance(project);
            cls = psiFacade.createClassByQName(name, anchor);
            cache.put(name, cls);
        }
        return cls;
    }

    @Nullable
    public static PyFunction findMethodByName(@Nullable String name, @Nullable PyClass pyClass, @NotNull TypeEvalContext context) {
        Map<String, PyFunction> cache = CachedValuesManager.getCachedValue(pyClass, () -> {
            return CachedValueProvider.Result.create(new HashMap<>(), pyClass);
        });
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        PyFunction method = pyClass.findMethodByName(name, false, context);
        cache.put(name, method);
        return method;
    }

    @Nullable
    public static PyTargetExpression findClassAttribute(@Nullable String name, @Nullable PyClass pyClass, @NotNull TypeEvalContext context) {
        Map<String, PyTargetExpression> cache = CachedValuesManager.getCachedValue(pyClass, () -> {
            return CachedValueProvider.Result.create(new HashMap<>(), pyClass);
        });
        if (cache.containsKey(name)) {
            return cache.get(name);
        }
        PyTargetExpression attribute = pyClass.findClassAttribute(name, false, context);
        cache.put(name, attribute);
        return attribute;
    }
}
