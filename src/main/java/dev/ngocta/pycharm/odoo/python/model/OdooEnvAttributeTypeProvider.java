package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.PyUnionType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.OdooPyNames;
import dev.ngocta.pycharm.odoo.python.OdooPyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

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
            if (isEnvironmentType(operandType, callSite)) {
                PyExpression index = subscription.getIndexExpression();
                if (index instanceof PyLiteralExpression) {
                    String model = ((PyStringLiteralExpressionImpl) index).getStringValue();
                    OdooModelClassType modelClassType = OdooModelClassType.create(model, OdooRecordSetType.MODEL, project);
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
            if (isEnvironmentType(qualifierType, referenceExpression)) {
                if (OdooPyNames.USER.equals(referenceName)) {
                    return OdooModelClassType.create(OdooPyNames.RES_USERS, OdooRecordSetType.MODEL, project);
                } else if (OdooPyNames.CONTEXT.equals(referenceName)) {
                    return OdooPyUtils.getContextType(referenceExpression);
                } else if (OdooPyNames.UID.equals(referenceName)) {
                    return builtinCache.getIntType();
                } else if (OdooPyNames.CR.equals(referenceName)) {
                    return OdooPyUtils.getDbCursorType(referenceExpression);
                }
            }
        }
        return null;
    }

    private boolean isEnvironmentType(@Nullable PyType type, @NotNull PsiElement anchor) {
        if (type == null) {
            return false;
        }
        Collection<PyType> candidateTypes;
        if (type instanceof PyUnionType) {
            candidateTypes = ((PyUnionType) type).getMembers();
        } else {
            candidateTypes = Collections.singleton(type);
        }
        return candidateTypes.stream().anyMatch(candidate -> {
            return candidate.equals(OdooPyUtils.getEnvironmentType(anchor));
        });
    }
}
