package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.model.OdooRecordSetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooEnvAttributeTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getCallType(@NotNull PyFunction function, @NotNull PyCallSiteExpression callSite, @NotNull TypeEvalContext context) {
        String functionName = function.getName();
        if (PyNames.GETITEM.equals(functionName) && callSite instanceof PySubscriptionExpression) {
            PySubscriptionExpression subscription = (PySubscriptionExpression) callSite;
            Project project = subscription.getProject();
            PyExpression operand = subscription.getOperand();
            PyType operandType = context.getType(operand);
            if (OdooTypeUtils.isEnvironmentType(operandType, callSite)) {
                PyExpression index = subscription.getIndexExpression();
                if (index instanceof PyLiteralExpression) {
                    String model = ((PyStringLiteralExpressionImpl) index).getStringValue();
                    OdooModelClassType modelClassType = new OdooModelClassType(model, OdooRecordSetType.MODEL, project);
                    return Ref.create(modelClassType);
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression, @NotNull TypeEvalContext context) {
        Project project = referenceExpression.getProject();
        String referenceName = referenceExpression.getName();
        PyExpression qualifier = referenceExpression.getQualifier();
        if (qualifier != null) {
            PyType qualifierType = context.getType(qualifier);
            PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(referenceExpression);
            if (OdooTypeUtils.isEnvironmentType(qualifierType, referenceExpression)) {
                if (OdooNames.ENV_USER.equals(referenceName)) {
                    return new OdooModelClassType(OdooNames.RES_USERS, OdooRecordSetType.MODEL, project);
                } else if (OdooNames.ENV_CONTEXT.equals(referenceName)) {
                    return OdooTypeUtils.getContextType(referenceExpression);
                } else if (OdooNames.ENV_UID.equals(referenceName)) {
                    return builtinCache.getIntType();
                } else if (OdooNames.ENV_CR.equals(referenceName)) {
                    return OdooTypeUtils.getDbCursorType(referenceExpression);
                }
            }
        }
        return null;
    }

}