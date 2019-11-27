package dev.ngocta.pycharm.odoo.python.model;

import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyFunctionImpl;
import com.jetbrains.python.psi.types.PyCollectionTypeImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.OdooPyNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;

public class OdooModelFunction {
    public static PyFunction wrap(@NotNull PyFunction origin, @NotNull OdooModelClassType modelClassType) {
        String name = origin.getName();
        if (name != null) {
            switch (name) {
                case PyNames.GETITEM:
                case PyNames.ITER:
                case OdooPyNames.BROWSE:
                case OdooPyNames.CREATE:
                case OdooPyNames.SUDO:
                case OdooPyNames.MAPPED:
                case OdooPyNames.FILTERED:
                case OdooPyNames.SORTED:
                case OdooPyNames.SEARCH:
                case OdooPyNames.WITH_CONTEXT:
                case OdooPyNames.WITH_ENV:
                    return new Wrapper(origin, modelClassType);
            }
        }
        return origin;
    }

    public static class Wrapper extends PyFunctionImpl {
        OdooModelClassType myModelClassType;

        private Wrapper(@NotNull PyFunction origin, @NotNull OdooModelClassType modelClassType) {
            super(origin.getNode());
            myModelClassType = modelClassType;
        }

        @Nullable
        @Override
        public PyType getReturnType(@NotNull TypeEvalContext context, @NotNull TypeEvalContext.Key key) {
            return myModelClassType;
        }

        @Nullable
        @Override
        public PyType getCallType(@NotNull TypeEvalContext context, @NotNull PyCallSiteExpression callSite) {
            String name = getName();
            if (name != null && callSite instanceof PyCallExpression) {
                PyCallExpression callExpression = ((PyCallExpression) callSite);
                if (OdooPyNames.MAPPED.equals(name)) {
                    PyType fieldType = getMappedParamType(callExpression, context);
                    if (fieldType != null) {
                        if (fieldType instanceof OdooModelClassType) {
                            return ((OdooModelClassType) fieldType).getMultiRecordVariant();
                        } else {
                            PyClass cls = PyBuiltinCache.getInstance(callSite).getClass("list");
                            if (cls != null) {
                                return new PyCollectionTypeImpl(cls, false, Collections.singletonList(fieldType));
                            }
                        }
                    }
                    return null;
                }
            }
            return myModelClassType;
        }

        @Nullable
        private PyType getMappedParamType(@NotNull PyCallExpression mapped, @NotNull TypeEvalContext context) {
            PyStringLiteralExpression fieldPathExpression = mapped.getArgument(0, PyStringLiteralExpression.class);
            if (fieldPathExpression != null) {
                String fieldPath = fieldPathExpression.getStringValue();
                return myModelClassType.getFieldTypeByPath(fieldPath, context);
            }
            return null;
        }
    }
}
