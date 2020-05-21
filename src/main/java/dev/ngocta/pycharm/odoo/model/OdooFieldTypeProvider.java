package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiNamedElement;
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
        PyExpression qualifier = referenceExpression.getQualifier();
        if (referenceName != null && qualifier != null && context.getOrigin() != null) {
            PyType qualifierType = context.getType(qualifier);
            OdooModelClassType modelType = OdooModelUtils.extractOdooModelClassType(qualifierType);
            if (modelType != null && modelType.getRecordSetType() != OdooRecordSetType.NONE) {
                Ref<PyType> ref = new Ref<>();
                modelType.visitMembers(element -> {
                    if (element instanceof PsiNamedElement && referenceName.equals(((PsiNamedElement) element).getName())) {
                        if (element instanceof PyTargetExpression) {
                            PyType type = OdooFieldInfo.getFieldType((PyTargetExpression) element, context);
                            if (type != null) {
                                ref.set(type);
                            }
                        }
                        return false;
                    }
                    return true;
                }, true, context);
                return ref.get();
            }
        }
        return null;
    }
}
