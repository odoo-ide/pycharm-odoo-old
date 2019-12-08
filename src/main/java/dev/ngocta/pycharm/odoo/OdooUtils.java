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
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyPsiFacade;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OdooUtils {
    @Nullable
    public static VirtualFile getOdooModuleDirectory(@NotNull VirtualFile file) {
        VirtualFile cur = file;
        while (cur != null) {
            if (cur.findChild(OdooNames.MANIFEST_FILE_NAME) != null) {
                return cur;
            }
            cur = cur.getParent();
        }
        return null;
    }

    @Nullable
    public static PsiDirectory getOdooModule(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file != null) {
            VirtualFile virtualFile = file.getVirtualFile();
            if (virtualFile == null) {
                virtualFile = file.getOriginalFile().getVirtualFile();
            }
            VirtualFile dir = getOdooModuleDirectory(virtualFile);
            if (dir != null) {
                return PsiManager.getInstance(element.getProject()).findDirectory(dir);
            }
        }
        return null;
    }

    public static boolean isOdooModelFile(@Nullable PsiFile file) {
        return file instanceof PyFile && getOdooModuleDirectory(file.getVirtualFile()) != null;
    }

    @Nullable
    public static PyClass getClassByQName(@NotNull String name, @NotNull PsiElement anchor) {
        PyPsiFacade psiFacade = PyPsiFacade.getInstance(anchor.getProject());
        return psiFacade.createClassByQName(name, anchor);
    }
}
