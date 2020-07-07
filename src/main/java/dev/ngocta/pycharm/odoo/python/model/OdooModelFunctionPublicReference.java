package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class OdooModelFunctionPublicReference extends OdooModelFunctionReference {
    public OdooModelFunctionPublicReference(@NotNull PsiElement element,
                                            @NotNull String model) {
        super(element, model);
    }

    @Override
    protected Collection<PyFunction> getFunctions() {
        Collection<PyFunction> functions = super.getFunctions();
        functions.removeIf(function -> {
            String name = function.getName();
            return name != null && name.startsWith("_");
        });
        return functions;
    }
}
