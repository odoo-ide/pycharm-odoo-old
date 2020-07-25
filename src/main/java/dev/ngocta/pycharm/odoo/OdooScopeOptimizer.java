package dev.ngocta.pycharm.odoo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.ScopeOptimizer;
import com.intellij.psi.search.SearchScope;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleIndex;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class OdooScopeOptimizer implements ScopeOptimizer {
    @Override
    @Nullable("is null when given optimizer can't provide a scope to restrict")
    public SearchScope getRestrictedUseScope(@NotNull PsiElement element) {
        OdooModule containingOdooModule = OdooModuleUtils.getContainingOdooModule(element);
        if (containingOdooModule != null) {
            List<GlobalSearchScope> scopes = new LinkedList<>();
            scopes.add(containingOdooModule.getOdooModuleScope(false));
            List<OdooModule> odooModules = OdooModuleIndex.getAvailableOdooModules(element);
            for (OdooModule module : odooModules) {
                if (module.isDependOn(containingOdooModule)) {
                    scopes.add(module.getOdooModuleScope(false));
                }
            }
            return GlobalSearchScope.union(scopes);
        }
        return null;
    }
}
