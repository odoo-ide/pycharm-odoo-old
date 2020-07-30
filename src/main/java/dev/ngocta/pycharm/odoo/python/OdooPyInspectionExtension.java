package dev.ngocta.pycharm.odoo.python;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.python.inspections.PyInspectionExtension;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyExpressionStatement;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

public class OdooPyInspectionExtension extends PyInspectionExtension {
    public boolean ignoreUnresolvedReference(@NotNull PyElement node,
                                             @NotNull PsiReference reference,
                                             @NotNull TypeEvalContext context) {
        if (context.getOrigin() instanceof XmlFile && node instanceof PyReferenceExpression) {
            PyReferenceExpression referenceExpression = (PyReferenceExpression) node;
            PyExpression qualifier = referenceExpression.getQualifier();
            if (qualifier == null) {
                return true;
            } else if (qualifier instanceof PyReferenceExpression) {
                referenceExpression = (PyReferenceExpression) qualifier;
                return referenceExpression.getQualifier() == null && "object".equals(referenceExpression.getName());
            }
        }
        return false;
    }

    @Override
    public boolean ignoreNoEffectStatement(@NotNull PyExpressionStatement expressionStatement) {
        PsiFile file = expressionStatement.getContainingFile();
        if (file == null) {
            return true;
        }
        if (file.getContext() != null) {
            return true;
        }
        if (OdooNames.MANIFEST_FILE_NAME.equals(file.getName())) {
            return true;
        }
        return super.ignoreNoEffectStatement(expressionStatement);
    }
}
