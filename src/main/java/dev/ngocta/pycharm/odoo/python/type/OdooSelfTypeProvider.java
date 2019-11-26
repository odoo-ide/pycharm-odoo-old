package dev.ngocta.pycharm.odoo.python.type;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.python.model.OdooRecordSetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooSelfTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getParameterType(@NotNull PyNamedParameter param, @NotNull PyFunction function, @NotNull TypeEvalContext context) {
        if (param.isSelf()) {
            PyClass pyClass = PyUtil.getContainingClassOrSelf(param);
            if (pyClass != null) {
                PsiElement parent = param.getParent();
                if (parent instanceof PyParameterList) {
                    PyParameterList parameterList = (PyParameterList) parent;
                    PyFunction func = parameterList.getContainingFunction();
                    if (func != null) {
                        final PyFunction.Modifier modifier = func.getModifier();
                        OdooRecordSetType recordSetType = modifier == PyFunction.Modifier.CLASSMETHOD ? OdooRecordSetType.NONE : OdooRecordSetType.MULTI;
                        OdooModelClassType type = OdooModelClassType.create(pyClass, recordSetType);
                        if (type != null) {
                            return Ref.create(type);
                        }
                    }
                }
            }
        }
        return null;
    }
}
