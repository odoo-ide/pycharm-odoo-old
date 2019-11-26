package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.OdooPyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooFieldTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression, @NotNull TypeEvalContext context) {
        PsiPolyVariantReference variantReference = referenceExpression.getReference();
        PsiElement psiElement = variantReference.resolve();
        if (psiElement instanceof PyTargetExpression) {
            return OdooPyUtils.getModelFieldType((PyTargetExpression) psiElement, context);
        }
        return null;
    }
}
