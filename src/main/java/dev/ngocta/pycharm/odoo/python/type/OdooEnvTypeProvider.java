package dev.ngocta.pycharm.odoo.python.type;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;
import com.jetbrains.python.psi.types.*;
import dev.ngocta.pycharm.odoo.python.OdooNames;
import dev.ngocta.pycharm.odoo.python.OdooUtils;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.python.model.OdooRecordSetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class OdooEnvTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getCallType(@NotNull PyFunction function, @NotNull PyCallSiteExpression callSite, @NotNull TypeEvalContext context) {
        String functionName = function.getName();
        if (PyNames.GETITEM.equals(functionName) && callSite instanceof PySubscriptionExpression) {
            PySubscriptionExpression subscription = (PySubscriptionExpression) callSite;
            return getTypeFromEnvExpression(subscription, context);
        }
        return null;
    }

    @Nullable
    private Ref<PyType> getTypeFromEnvExpression(PySubscriptionExpression expression, TypeEvalContext context) {
        Project project = expression.getProject();
        PyExpression operand = expression.getOperand();
        PyType operandType = context.getType(operand);
        Collection<PyType> candidateTypes;
        if (operandType instanceof PyUnionType) {
            candidateTypes = ((PyUnionType) operandType).getMembers();
        } else {
            candidateTypes = Collections.singleton(operandType);
        }
        for (PyType candidateType : candidateTypes) {
            if (candidateType instanceof PyClassType) {
                PyClassType classType = (PyClassType) candidateType;
                if (OdooNames.ENVIRONMENT_QNAME.equals(classType.getClassQName())) {
                    PyExpression index = expression.getIndexExpression();
                    if (index instanceof PyLiteralExpression) {
                        String model = ((PyStringLiteralExpressionImpl) index).getStringValue();
                        OdooModelClassType modelClassType = OdooModelClassType.create(model, OdooRecordSetType.MODEL, project);
                        return Ref.create(modelClassType);
                    }
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
            if (OdooNames.ENV.equals(referenceName) && qualifierType instanceof OdooModelClassType) {
                PyClass envClass = OdooUtils.createClassByQName(OdooNames.ENVIRONMENT_QNAME, referenceExpression);
                if (envClass != null) {
                    return new PyClassTypeImpl(envClass, false);
                }
            } else if (OdooNames.USER.equals(referenceName) && qualifierType instanceof PyClassType) {
                if (OdooNames.ENVIRONMENT_QNAME.equals(((PyClassType) qualifierType).getClassQName())) {
                    return OdooModelClassType.create(OdooNames.RES_USERS, OdooRecordSetType.MODEL, project);
                }
            }
        }
        return null;
    }
}
