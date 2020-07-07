package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.util.Ref;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyCollectionTypeImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
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
            PyCallExpression mapped = (PyCallExpression) callSite;
            PyType qualifierType = getCalleeQualifierType(mapped, context);
            if (qualifierType instanceof OdooModelClassType) {
                Ref<PyType> result = new Ref<>();
                PyStringLiteralExpression fieldPathExpression = mapped.getArgument(0, PyStringLiteralExpression.class);
                if (fieldPathExpression != null) {
                    String fieldPath = fieldPathExpression.getStringValue();
                    PyType fieldType = ((OdooModelClassType) qualifierType).getFieldTypeByPath(fieldPath, context);
                    if (fieldType != null) {
                        if (fieldType instanceof OdooModelClassType) {
                            result.set(((OdooModelClassType) fieldType).withMultiRecord());
                        } else {
                            PyClass cls = PyBuiltinCache.getInstance(callSite).getClass("list");
                            if (cls != null) {
                                result.set(new PyCollectionTypeImpl(cls, false, Collections.singletonList(fieldType)));
                            }
                        }
                    }
                }
                return result;
            }
            return null;
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
