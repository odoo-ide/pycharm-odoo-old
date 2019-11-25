package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyClassLikeType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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
        ConcurrentMap<String, PyClass> cache = CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            return CachedValueProvider.Result.create(new ConcurrentHashMap<>(), ModificationTracker.NEVER_CHANGED);
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
    public static PyFunction findMethodByName(@NotNull String name, @NotNull PyClass pyClass, @NotNull TypeEvalContext context) {
        PsiElement member = findClassMember(name, pyClass, context);
        if (member instanceof PyFunction) {
            return (PyFunction) member;
        }
        return null;
    }

    @Nullable
    public static PyTargetExpression findClassAttribute(@NotNull String name, @NotNull PyClass pyClass, @NotNull TypeEvalContext context) {
        PsiElement member = findClassMember(name, pyClass, context);
        if (member instanceof PyTargetExpression) {
            return (PyTargetExpression) member;
        }
        return null;
    }

    @Nullable
    public static PsiElement findClassMember(@NotNull String name, @NotNull PyClass pyClass, @NotNull TypeEvalContext context) {
        Map<String, PsiElement> cache = CachedValuesManager.getCachedValue(pyClass, () -> {
            Map<String, PsiElement> members = new HashMap<>();
            pyClass.processClassLevelDeclarations((element, state) -> {
                if (element instanceof PsiNamedElement) {
                    members.put(((PsiNamedElement) element).getName(), element);
                }
                return true;
            });
            return CachedValueProvider.Result.create(members, pyClass);
        });
        return cache.get(name);
    }
}
