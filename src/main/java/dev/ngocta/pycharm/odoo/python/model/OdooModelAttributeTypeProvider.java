package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.types.PyClassTypeImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.OdooPyNames;
import dev.ngocta.pycharm.odoo.python.OdooPyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelAttributeTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression, @NotNull TypeEvalContext context) {
        String referenceName = referenceExpression.getName();
        PyExpression qualifier = referenceExpression.getQualifier();
        if (qualifier != null) {
            PyType qualifierType = context.getType(qualifier);
            OdooModelClassType modelType = OdooPyUtils.unpackType(qualifierType, OdooModelClassType.class);
            if (modelType != null) {
                PyType type = modelType.getImplicitAttributeTypes(context).get(referenceName);
                if (type == null) {
                    PsiPolyVariantReference variantReference = referenceExpression.getReference();
                    PsiElement psiElement = variantReference.resolve();
                    if (psiElement instanceof PyTargetExpression) {
                        type = OdooPyUtils.getModelFieldType((PyTargetExpression) psiElement, context);
                    }
                }
                return type;
            }
        }
        return null;
    }
}
