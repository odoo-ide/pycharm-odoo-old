package dev.ngocta.pycharm.odoo.python;

import com.intellij.openapi.util.Ref;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.types.PyClassTypeImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooSelfTypeProvider extends PyTypeProviderBase {
    @Override
    @Nullable
    public Ref<PyType> getParameterType(@NotNull PyNamedParameter param,
                                        @NotNull PyFunction func,
                                        @NotNull TypeEvalContext context) {
        if (param.isSelf()) {
            final PyClass containingClass = func.getContainingClass();
            if (containingClass != null && !OdooModelUtils.isInOdooModelClass(containingClass)) {
                final PyFunction.Modifier modifier = func.getModifier();
                return Ref.create(new PyClassTypeImpl(containingClass, modifier == PyFunction.Modifier.CLASSMETHOD));
            }
        }
        return super.getParameterType(param, func, context);
    }
}
