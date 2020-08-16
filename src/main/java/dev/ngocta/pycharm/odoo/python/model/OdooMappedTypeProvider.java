package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.python.OdooPyUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class OdooMappedTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getCallType(@NotNull PyFunction function,
                                   @NotNull PyCallSiteExpression callSite,
                                   @NotNull TypeEvalContext context) {
        if (OdooNames.MAPPED.equals(function.getName()) && callSite instanceof PyCallExpression) {
            PyCallExpression mappedCall = (PyCallExpression) callSite;
            PyType qualifierType = getCalleeQualifierType(mappedCall, context);
            OdooModelClassType modelClassType = OdooModelUtils.extractOdooModelClassType(qualifierType);
            if (modelClassType == null) {
                return null;
            }
            Ref<PyType> result = new Ref<>();
            PyStringLiteralExpression fieldPathExpression = mappedCall.getArgument(0, PyStringLiteralExpression.class);
            if (fieldPathExpression != null) {
                String fieldPath = fieldPathExpression.getStringValue();
                PsiElement field = modelClassType.getPyClass().findFieldByPath(fieldPath, context);
                PyType fieldType = OdooFieldInfo.getFieldType(field, context);
                if (fieldType instanceof OdooModelClassType) {
                    result.set(((OdooModelClassType) fieldType).withMultiRecord());
                } else if (fieldType != null) {
                    result.set(OdooPyUtils.getListType(Collections.singletonList(fieldType), false, function));
                }
            }
            return result;
        }
        return null;
    }

    @Nullable
    private PyType getCalleeQualifierType(@NotNull PyCallExpression call,
                                          @NotNull TypeEvalContext context) {
        PyExpression callee = call.getCallee();
        if (callee instanceof PyReferenceExpression) {
            PyExpression qualifier = ((PyReferenceExpression) callee).getQualifier();
            if (qualifier != null) {
                return context.getType(qualifier);
            }
        }
        return null;
    }
}
