package dev.ngocta.pycharm.odoo.model;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.*;
import com.intellij.util.PlatformIcons;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
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
            return OdooModelIndex.findModelClasses(getValue(), getElement(), true);
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
        List<LookupElement> elements = new LinkedList<>();
        Set<String> models = OdooModelIndex.getAvailableModels(getElement());
        models.forEach(model -> {
            LookupElement element = LookupElementBuilder.create(model).withIcon(PlatformIcons.CLASS_ICON);
            elements.add(element);
        });
        return elements.toArray();
    }
}
