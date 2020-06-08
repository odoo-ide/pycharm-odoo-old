package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class OdooUtils {
    private OdooUtils() {

    }

    @NotNull
    public static GlobalSearchScope getProjectModuleAndDependenciesScope(@NotNull PsiElement anchor) {
        Module module = ModuleUtil.findModuleForPsiElement(anchor);
        if (module != null) {
            return module.getModuleContentWithDependenciesScope().union(module.getModuleWithLibrariesScope());
        }
        return GlobalSearchScope.projectScope(anchor.getProject());
    }
}
