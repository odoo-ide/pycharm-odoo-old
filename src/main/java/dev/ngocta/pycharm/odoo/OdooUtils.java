package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.PyNames;
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

    public static boolean isOdooModelFile(@Nullable VirtualFile file) {
        return file != null && file.getName().endsWith(PyNames.DOT_PY) && getOdooModuleDir(file) != null;
    }

    public static boolean isOdooModelFile(@Nullable PsiFile file) {
        return file instanceof PyFile && getOdooModuleDir(file.getVirtualFile()) != null;
    }
}
