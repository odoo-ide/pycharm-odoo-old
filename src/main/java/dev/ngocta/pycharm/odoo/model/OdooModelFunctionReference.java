package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelFunctionReference extends PsiReferenceBase<PsiElement> implements PsiReference {
    private final OdooModelClass myModelClass;

    public OdooModelFunctionReference(@NotNull PsiElement element, @NotNull String model) {
        this(element, OdooModelClass.getInstance(model, element.getProject()));
    }

    public OdooModelFunctionReference(@NotNull PsiElement element, @NotNull OdooModelClass cls) {
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
        TypeEvalContext context = TypeEvalContext.codeCompletion(getElement().getProject(), getElement().getContainingFile());
        return myModelClass.getMethods(context).toArray();
    }
}
