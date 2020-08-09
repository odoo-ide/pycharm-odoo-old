package dev.ngocta.pycharm.odoo.python;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.PythonParserDefinition;
import com.jetbrains.python.psi.impl.*;
import dev.ngocta.pycharm.odoo.python.psi.*;
import org.jetbrains.annotations.NotNull;

public class OdooPythonParserDefinition extends PythonParserDefinition {
    @NotNull
    @Override
    public PsiElement createElement(@NotNull ASTNode node) {
        PsiElement element = super.createElement(node);
        if (element instanceof PyReferenceExpressionImpl) {
            element = new OdooPyReferenceExpression(node);
        } else if (element instanceof PySubscriptionExpressionImpl) {
            element = new OdooPySubscriptionExpression(node);
        } else if (element instanceof PySliceExpressionImpl) {
            element = new OdooPySliceExpression(node);
        } else if (element instanceof PyCallExpressionImpl) {
            element = new OdooPyCallExpression(node);
        } else if (element instanceof PyStringLiteralExpressionImpl) {
            element = new OdooPyStringLiteralExpression(node);
        }
        return element;
    }
}
