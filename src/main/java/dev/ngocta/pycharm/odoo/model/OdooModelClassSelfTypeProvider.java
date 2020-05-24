package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.Ref;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelClassSelfTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getParameterType(@NotNull PyNamedParameter param,
                                        @NotNull PyFunction function,
                                        @NotNull TypeEvalContext context) {
        if (param.isSelf()) {
            OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(function);
            if (modelClass != null) {
                final PyFunction.Modifier modifier = function.getModifier();
                OdooRecordSetType recordSetType =
                        modifier == PyFunction.Modifier.CLASSMETHOD ? OdooRecordSetType.NONE : OdooRecordSetType.MULTI;
                OdooModelClassType type = new OdooModelClassType(modelClass, recordSetType);
                return Ref.create(type);
            }
        }
        return null;
    }
}
