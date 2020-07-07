package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.psi.PsiFile;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.Nullable;

public class OdooJSUtils {
    private OdooJSUtils() {
    }

    public static boolean isOdooJSFile(@Nullable PsiFile file) {
        return file instanceof JSFile && OdooModuleUtils.isInOdooModule(file);
    }
}
