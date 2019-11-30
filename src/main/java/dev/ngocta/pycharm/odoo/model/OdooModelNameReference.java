package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.*;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyStringLiteralExpression;
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
        List<PyClass> targets = OdooModelIndex.findModelClasses(model, getElement(), true);
        Collections.reverse(targets);
        return PsiElementResolveResult.createResults(targets);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return null;
    }
}
