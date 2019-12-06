package dev.ngocta.pycharm.odoo.xml;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;

public class OdooXmlIdReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    public OdooXmlIdReference(@NotNull PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Collection<OdooRecordDefinition> definitions = OdooXmlIdIndex.findRecordDefinitions(getValue(), getElement().getProject());
        Collection<PsiElement> elements = new LinkedList<>();
        definitions.forEach(def -> elements.add(def.getNavigationElement()));
        return PsiElementResolveResult.createResults(elements);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return null;
    }
}
