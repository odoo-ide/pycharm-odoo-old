package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooFieldTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression,
                                             @NotNull TypeEvalContext context) {
        String referenceName = referenceExpression.getName();
        if (referenceName == null) {
            return null;
        }
        PyExpression qualifier = referenceExpression.getQualifier();
        if (qualifier == null) {
            return null;
        }
        PyType qualifierType = context.getType(qualifier);
        if (qualifierType != null) {
            OdooModelClassType modelType = OdooModelUtils.extractOdooModelClassType(qualifierType);
            if (modelType != null && modelType.getRecordSetType() != OdooRecordSetType.NONE) {
                Ref<PyType> ref = new Ref<>();
                PsiElement element = modelType.resolvePsiMember(referenceName, context);
                if (element instanceof PyTargetExpression) {
                    PyType type = OdooFieldInfo.getFieldType((PyTargetExpression) element, context);
                    ref.set(type);
                }
                return ref.get();
            }
            return null;
        }
        return OdooModelUtils.guessFieldTypeByName(referenceName, referenceExpression, context);
    }
}
