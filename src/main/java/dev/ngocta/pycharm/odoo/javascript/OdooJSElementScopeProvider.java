package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.psi.resolve.JSElementResolveScopeProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooJSElementScopeProvider implements JSElementResolveScopeProvider {
    @Nullable
    @Override
    public GlobalSearchScope getElementResolveScope(@NotNull PsiElement psiElement) {
        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(psiElement);
        if (odooModule != null) {
            return odooModule.getSearchScope();
        }
        return null;
    }
}
