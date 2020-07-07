package dev.ngocta.pycharm.odoo.python;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.PyParameter;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooMigrateFunctionParamTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getParameterType(@NotNull PyNamedParameter param,
                                        @NotNull PyFunction func,
                                        @NotNull TypeEvalContext context) {
        String funcName = func.getName();
        if (!"migrate".equals(funcName)) {
            return null;
        }
        PsiFile file = context.getOrigin();
        if (file == null) {
            return null;
        }
        PsiDirectory dir = file.getParent();
        if (dir == null) {
            return null;
        }
        dir = dir.getParent();
        if (dir == null) {
            return null;
        }
        if (!"migrations".equals(dir.getName())) {
            return null;
        }
        OdooModule module = OdooModuleUtils.getContainingOdooModule(dir);
        if (module == null) {
            return null;
        }
        PyParameter[] parameters = func.getParameterList().getParameters();
        if (parameters.length > 0 && parameters[0] == param) {
            return Ref.create(OdooPyUtils.getDbCursorType(dir));
        } else if (parameters.length > 1 && parameters[1] == param) {
            return Ref.create(PyBuiltinCache.getInstance(func).getStrType());
        }
        return null;
    }
}
