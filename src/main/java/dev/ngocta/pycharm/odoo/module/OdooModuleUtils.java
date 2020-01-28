package dev.ngocta.pycharm.odoo.module;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.sun.istack.Nullable;
import dev.ngocta.pycharm.odoo.OdooNames;

public class OdooModuleUtils {
    private OdooModuleUtils() {
    }

    public static boolean isInOdooModule(@Nullable PsiElement element) {
        return OdooModule.findModule(element) != null;
    }

    public static String getManifestFileName(@Nullable Project project) {
        return OdooNames.MANIFEST_FILE_NAME;
    }
}
