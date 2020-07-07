package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModelFunctionReference extends PsiReferenceBase<PsiElement> implements PsiReference {
    private final OdooModelClass myModelClass;

    public OdooModelFunctionReference(@NotNull PsiElement element,
                                      @NotNull String model) {
        this(element, OdooModelClass.getInstance(model, element.getProject()));
    }

    public OdooModelFunctionReference(@NotNull PsiElement element,
                                      @NotNull OdooModelClass cls) {
        super(element);
        myModelClass = cls;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return PyUtil.getNullableParameterizedCachedValue(getElement(), null, param -> {
            TypeEvalContext context = TypeEvalContext.codeAnalysis(getElement().getProject(), getElement().getContainingFile());
            return myModelClass.findMethodByName(getValue(), true, context);
        });
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return getFunctions().toArray();
    }

    protected Collection<PyFunction> getFunctions() {
        TypeEvalContext context = TypeEvalContext.codeCompletion(getElement().getProject(), getElement().getContainingFile());
        List<PyFunction> functions = new LinkedList<>();
        Set<String> visitedNames = new HashSet<>();
        myModelClass.visitMethods(function -> {
            String name = function.getName();
            if (name != null && !visitedNames.contains(name)) {
                functions.add(function);
                visitedNames.add(name);
            }
            return true;
        }, true, context);
        return functions;
    }
}
