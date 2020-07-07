package dev.ngocta.pycharm.odoo.python;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.PyParameter;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooInitHookFunctionParamTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getParameterType(@NotNull PyNamedParameter param,
                                        @NotNull PyFunction func,
                                        @NotNull TypeEvalContext context) {
        String funcName = func.getName();
        if (funcName == null) {
            return null;
        }
        PsiFile file = context.getOrigin();
        if (file == null) {
            return null;
        }
        if (!PyNames.INIT_DOT_PY.equals(file.getName())) {
            return null;
        }
        PsiDirectory dir = file.getContainingDirectory();
        if (dir == null) {
            return null;
        }
        if (dir.findFile(OdooNames.MANIFEST_FILE_NAME) == null) {
            return null;
        }
        PyParameter[] parameters = func.getParameterList().getParameters();
        if (parameters.length > 0 && parameters[0] == param && "cr".equals(param.getName())) {
            return Ref.create(OdooPyUtils.getDbCursorType(dir));
        } else if (parameters.length > 1 && parameters[1] == param && param.getName() != null && param.getName().startsWith("reg")) {
            return Ref.create(OdooPyUtils.getRegistryType(dir));
        }
        return null;
    }
}
