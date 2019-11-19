package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.psi.PyFile;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

public class OdooUtils {
    @Nullable
    public static VirtualFile getOdooModuleDir(@NotNull VirtualFile file) {
        VirtualFile cur = file;
        while (cur != null) {
            if (cur.findChild(OdooNames.MANIFEST) != null) {
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
}
