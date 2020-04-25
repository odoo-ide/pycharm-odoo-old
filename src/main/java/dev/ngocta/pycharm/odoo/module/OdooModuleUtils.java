package dev.ngocta.pycharm.odoo.module;

import com.intellij.psi.PsiElement;
import com.sun.istack.Nullable;

public class OdooModuleUtils {
    private OdooModuleUtils() {
    }

    public static boolean isInOdooModule(@Nullable PsiElement element) {
        return OdooModule.findModule(element) != null;
    }
}
