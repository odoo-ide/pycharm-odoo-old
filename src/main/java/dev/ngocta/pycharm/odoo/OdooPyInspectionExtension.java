package dev.ngocta.pycharm.odoo;

import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.python.inspections.PyInspectionExtension;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyExpressionStatement;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

public class OdooPyInspectionExtension extends PyInspectionExtension {
    public boolean ignoreUnresolvedReference(@NotNull PyElement node,
                                             @NotNull PsiReference reference,
                                             @NotNull TypeEvalContext context) {
        if (node instanceof PyReferenceExpression) {
            return context.getOrigin() instanceof XmlFile;
        }
        return false;
    }

    @Override
    public boolean ignoreNoEffectStatement(@NotNull PyExpressionStatement expressionStatement) {
        PsiFile file = expressionStatement.getContainingFile();
        return file != null && OdooNames.MANIFEST_FILE_NAME.equals(file.getName());
    }
}
