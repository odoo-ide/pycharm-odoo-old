package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.search.PySuperMethodsSearch;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OdooModelSuperMethodsSearch implements QueryExecutor<PsiElement, PySuperMethodsSearch.SearchParameters> {
    @Override
    public boolean execute(@NotNull PySuperMethodsSearch.SearchParameters queryParameters,
                           @NotNull Processor<? super PsiElement> consumer) {
        final PyFunction func = queryParameters.getDerivedMethod();
        final String name = func.getName();
        if (name == null) {
            return false;
        }
        final PyClass containingClass = func.getContainingClass();
        if (containingClass == null) {
            return false;
        }
        OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(containingClass);
        if (modelClass == null) {
            return true;
        }
        final TypeEvalContext context = queryParameters.getContext();
        List<PyFunction> methods = modelClass.multiFindMethodByName(name, true, context);
        methods.remove(func);
        for (PyFunction method : methods) {
            consumer.process(method);
        }
        return false;
    }
}
