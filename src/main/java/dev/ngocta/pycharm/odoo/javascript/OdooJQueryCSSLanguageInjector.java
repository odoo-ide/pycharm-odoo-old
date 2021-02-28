package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.frameworks.jquery.JQueryCssLanguage;
import com.intellij.lang.javascript.psi.JSArgumentList;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

public class OdooJQueryCSSLanguageInjector implements LanguageInjector {
    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host,
                                     @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
        if (host instanceof JSLiteralExpression && OdooModuleUtils.isInOdooModule(host)) {
            PsiElement parent = host.getParent();
            if (parent instanceof JSArgumentList) {
                parent = parent.getParent();
                if (parent instanceof JSCallExpression) {
                    String callExpressionName = OdooJSUtils.getCallFunctionName((JSCallExpression) parent);
                    if ("$".equals(callExpressionName) && OdooModuleUtils.isInOdooModule(parent)) {
                        TextRange range = ElementManipulators.getValueTextRange(host);
                        injectionPlacesRegistrar.addPlace(JQueryCssLanguage.INSTANCE, range, null, null);
                    }
                }
            }
        }
    }
}
