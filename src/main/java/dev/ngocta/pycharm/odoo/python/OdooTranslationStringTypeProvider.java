package dev.ngocta.pycharm.odoo.python;

import com.intellij.openapi.util.Ref;
import com.jetbrains.python.psi.PyCallSiteExpression;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooTranslationStringTypeProvider extends PyTypeProviderBase {
    @Override
    @Nullable
    public Ref<PyType> getCallType(@NotNull PyFunction function,
                                   @NotNull PyCallSiteExpression callSite,
                                   @NotNull TypeEvalContext context) {
        if (OdooPyUtils.isTranslationStringExpression(callSite)) {
            PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(callSite);
            return Ref.create(builtinCache.getStrType());
        }
        return super.getCallType(function, callSite, context);
    }
}
