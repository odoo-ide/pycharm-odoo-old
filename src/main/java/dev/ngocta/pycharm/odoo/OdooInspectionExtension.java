package dev.ngocta.pycharm.odoo;

import com.intellij.psi.PsiReference;
import com.jetbrains.python.inspections.PyInspectionExtension;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

public class OdooInspectionExtension extends PyInspectionExtension {
    public boolean ignoreUnresolvedReference(@NotNull PyElement node, @NotNull PsiReference reference, @NotNull TypeEvalContext context) {
        if (node instanceof PyQualifiedExpression) {
            PyExpression qualifier = ((PyQualifiedExpression) node).getQualifier();
            if (qualifier != null && context.getType(qualifier) instanceof OdooModelClassType) {
                String name = node.getName();
                return OdooNames.ENV.equals(name) || OdooNames.ID.equals(name);
            }
        }
        return false;
    }
}
