package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooComputeFunctionReference extends PsiReferenceBase<PyStringLiteralExpression> implements PsiReference {
    public OdooComputeFunctionReference(@NotNull PyStringLiteralExpression element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        PyClass cls = PsiTreeUtil.getParentOfType(getElement(), PyClass.class);
        if (cls != null) {
            TypeEvalContext context = TypeEvalContext.codeAnalysis(getElement().getProject(), getElement().getContainingFile());
            return cls.findMethodByName(getValue(), false, context);
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        PyClass cls = PsiTreeUtil.getParentOfType(getElement(), PyClass.class);
        if (cls != null) {
            return cls.getMethods();
        }
        return new Object[0];
    }
}
