package dev.ngocta.pycharm.odoo.python;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.highlighting.PyHighlighter;
import com.jetbrains.python.psi.PyExpressionStatement;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.validation.PyAnnotator;

public class OdooPyAnnotator extends PyAnnotator {
    @Override
    public void visitPyStringLiteralExpression(PyStringLiteralExpression node) {
        // Do not highlight python string literal as doc string when injected in xml file
        PsiElement parent = node.getParent();
        if (parent instanceof PyExpressionStatement) {
            parent = parent.getParent();
            if (parent instanceof PsiFile && ((PsiFile) parent).getName().endsWith(".xml")) {
                addHighlightingAnnotation(node, PyHighlighter.PY_UNICODE_STRING);
            }
        }
    }
}
