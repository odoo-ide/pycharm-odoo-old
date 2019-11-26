package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.PyUnionType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.OdooPyNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelFunctionParamTypeProvider extends PyTypeProviderBase {
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

        if (OdooPyNames.CREATE.equals(function.getName())) {
            PyClass cls = function.getContainingClass();
            PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(cls);
            if (cls != null && (OdooPyNames.BASE_MODEL_QNAME.equals(cls.getQualifiedName()) || OdooModelInfo.readFromClass(cls) != null)) {
                PyType type = PyUnionType.union(builtinCache.getListType(), builtinCache.getDictType());
                if (type != null) {
                    return Ref.create(type);
                }
            }
        }

        return null;
    }
}
