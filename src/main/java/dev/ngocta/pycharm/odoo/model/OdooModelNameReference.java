package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.*;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.PyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class OdooModelNameReference extends PsiReferenceBase<PyStringLiteralExpression> implements PsiPolyVariantReference {
    public OdooModelNameReference(@NotNull PyStringLiteralExpression element) {
        super(element);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        String model = getElement().getStringValue();
        PsiFile file = getElement().getContainingFile();
        if (file != null) {
            return PyUtil.getParameterizedCachedValue(file, model, modelArg -> {
                List<PyClass> targets = OdooModelIndex.findModelClasses(modelArg, getElement(), true);
                Collections.reverse(targets);
                return PsiElementResolveResult.createResults(targets);
            });
        }
        return new ResolveResult[0];
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return OdooModelIndex.getAllModels(getElement().getProject()).toArray();
    }
}
