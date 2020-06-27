package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.StructuredDocString;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.model.OdooModelIndex;
import dev.ngocta.pycharm.odoo.model.OdooRecordSetType;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import dev.ngocta.pycharm.odoo.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooPyDocStringTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getParameterType(@NotNull PyNamedParameter param,
                                        @NotNull PyFunction func,
                                        @NotNull TypeEvalContext context) {
        StructuredDocString docString = func.getStructuredDocString();
        if (docString != null) {
            String typeText = docString.getParamType(param.getName());
            if (typeText != null) {
                if (isOdooModelName(typeText, func)) {
                    return new Ref<>(new OdooModelClassType(typeText, OdooRecordSetType.MULTI, func.getProject()));
                }
            }
        }
        return null;
    }

    private static boolean isOdooModelName(@NotNull String name,
                                           @NotNull PsiElement anchor) {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(anchor);
        if (module == null) {
            return false;
        }
        Ref<Boolean> is = new Ref<>(false);
        FileBasedIndex.getInstance().processValues(OdooModelIndex.NAME, name, null, (file, value) -> {
            is.set(true);
            return false;
        }, module.getSearchScope());
        return is.get();
    }
}
