package dev.ngocta.pycharm.odoo.model;

import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyFunctionImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelGetItemWrapper extends PyFunctionImpl {
    final OdooModelClassType myModelClassType;

    public OdooModelGetItemWrapper(@NotNull PyFunction origin, @NotNull OdooModelClassType modelClassType) {
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
        if (callSite instanceof PySubscriptionExpression) {
            PyExpression index = ((PySubscriptionExpression) callSite).getIndexExpression();
            if (index instanceof PyStringLiteralExpression) {
                String fieldName = ((PyStringLiteralExpression) index).getStringValue();
                return myModelClassType.getFieldTypeByPath(new String[]{fieldName}, context);
            } else if (index instanceof PyNumericLiteralExpression) {
                return myModelClassType.withOneRecord();
            }
        }
        return null;
    }
}