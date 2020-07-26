package dev.ngocta.pycharm.odoo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.ScopeOptimizer;
import com.intellij.psi.search.SearchScope;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooScopeOptimizer implements ScopeOptimizer {
    @Override
    @Nullable("is null when given optimizer can't provide a scope to restrict")
    public SearchScope getRestrictedUseScope(@NotNull PsiElement element) {
        OdooModule containingOdooModule = OdooModuleUtils.getContainingOdooModule(element);
        if (containingOdooModule != null) {
            return containingOdooModule.getOdooModuleWithExtensionsScope();
        }
        return null;
    }
}
