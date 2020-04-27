package dev.ngocta.pycharm.odoo.model;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.*;
import com.intellij.util.PlatformIcons;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class OdooModelReference extends PsiReferenceBase.Poly<PsiElement> {
    public OdooModelReference(@NotNull PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return PsiElementResolveResult.createResults(resolveInner());
    }

    @NotNull
    protected List<PyClass> resolveInner() {
        return PyUtil.getParameterizedCachedValue(getElement(), null, param -> {
            PsiFile file = getElement().getContainingFile();
            if (file != null) {
                return OdooModelIndex.findModelClasses(getValue(), getElement());
            }
            return Collections.emptyList();
        });
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<LookupElement> elements = new LinkedList<>();
        Collection<String> models = OdooModelIndex.getAvailableModels(getElement());
        models.forEach(model -> {
            LookupElement element = LookupElementBuilder.create(model).withIcon(PlatformIcons.CLASS_ICON);
            elements.add(element);
        });
        return elements.toArray();
    }
}
