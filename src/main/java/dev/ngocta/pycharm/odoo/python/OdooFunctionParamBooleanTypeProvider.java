package dev.ngocta.pycharm.odoo.python;

import com.intellij.openapi.util.Ref;
import com.jetbrains.python.psi.PyBoolLiteralExpression;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooFunctionParamBooleanTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getParameterType(@NotNull PyNamedParameter param,
                                        @NotNull PyFunction func,
                                        @NotNull TypeEvalContext context) {
        if (param.getDefaultValue() instanceof PyBoolLiteralExpression) {
            // In Odoo, the default value for a recordset param is usually False,
            // that causes PyCharm infers its type is bool.
            // To avoid errors such as "Unresolved attribute reference xyz for class bool",
            // its type should be Any instead.
            return Ref.create();
        }
        return super.getParameterType(param, func, context);
    }
}
