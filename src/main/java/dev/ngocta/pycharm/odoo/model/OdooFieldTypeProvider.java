package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.types.*;
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
        if (referenceExpression.getParent() instanceof PyCallExpression) {
            return null;
        }
        PyExpression qualifier = referenceExpression.getQualifier();
        if (qualifier == null) {
            return null;
        }
        PyType qualifierType = context.getType(qualifier);
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
        if (isUnknownType(qualifierType) && (referenceName.endsWith("_id") || referenceName.endsWith("_ids"))) {
            return OdooModelUtils.guessFieldTypeByName(referenceName, referenceExpression, context);
        }
        return null;
    }

    private boolean isUnknownType(@Nullable PyType type) {
        if (type == null) {
            return true;
        }
        if (type instanceof PyStructuralType && ((PyStructuralType) type).isInferredFromUsages()) {
            return true;
        }
        if (type instanceof PyUnionType && ((PyUnionType) type).isWeak()) {
            return true;
        }
        return false;
    }
}
