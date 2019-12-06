package dev.ngocta.pycharm.odoo.xml;

import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class OdooXmlIdReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    public OdooXmlIdReference(@NotNull PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Collection<NavigationItem> definitions = OdooXmlIdIndex.findRecordDefinitions(getValue(), getElement().getProject());
        return new ResolveResult[0];
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return null;
    }
}
