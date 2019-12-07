package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.*;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class OdooModelReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    public OdooModelReference(@NotNull PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return PsiElementResolveResult.createResults(findModelClasses());
    }

    @NotNull
    protected List<PyClass> findModelClasses() {
        PsiFile file = getElement().getContainingFile();
        if (file != null) {
            return PyUtil.getParameterizedCachedValue(file, getValue(), model -> {
                return OdooModelIndex.findModelClasses(model, getElement(), true);
            });
        }
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return getVariantsInner().toArray();
    }

    @NotNull
    protected Set<String> getVariantsInner() {
        return OdooModelIndex.getAllModels(getElement());
    }
}
