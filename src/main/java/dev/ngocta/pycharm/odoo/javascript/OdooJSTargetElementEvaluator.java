package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.JSTargetElementEvaluator;
import com.intellij.lang.javascript.psi.*;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooJSTargetElementEvaluator extends JSTargetElementEvaluator {
    @Override
    @Nullable
    public PsiElement getNamedElement(@NotNull PsiElement element) {
        PsiElement parent = element.getParent();
        if (parent instanceof JSLiteralExpression) {
            String stringValue = ((JSLiteralExpression) parent).getStringValue();
            if (stringValue != null) {
                parent = parent.getParent();
                if (parent instanceof JSArgumentList) {
                    parent = parent.getParent();
                    if (parent instanceof JSCallExpression) {
                        JSExpression method = ((JSCallExpression) parent).getMethodExpression();
                        if (method instanceof JSReferenceExpression && "odoo.define".equals(method.getText())) {
                            return new OdooJSModule(stringValue, (JSCallExpression) parent);
                        }
                    }
                }
            }
        }
        return super.getNamedElement(element);
    }
}
