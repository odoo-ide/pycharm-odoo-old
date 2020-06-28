package dev.ngocta.pycharm.odoo;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.PythonParserDefinition;
import com.jetbrains.python.psi.impl.PyReferenceExpressionImpl;
import org.jetbrains.annotations.NotNull;

public class OdooPythonParserDefinition extends PythonParserDefinition {
    @NotNull
    @Override
    public PsiElement createElement(@NotNull ASTNode node) {
        PsiElement element = super.createElement(node);
        if (element instanceof PyReferenceExpressionImpl) {
            element = new OdooPyReferenceExpression(node);
        }
        return element;
    }
}
