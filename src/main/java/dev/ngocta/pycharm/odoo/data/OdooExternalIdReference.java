package dev.ngocta.pycharm.odoo.data;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.*;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class OdooExternalIdReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private final String myAcceptedModel;

    public OdooExternalIdReference(@NotNull PsiElement element) {
        super(element);
        myAcceptedModel = null;
    }

    public OdooExternalIdReference(@NotNull PsiElement element, @NotNull String acceptedModel) {
        super(element);
        myAcceptedModel = acceptedModel;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        Collection<OdooRecordItem> items = OdooExternalIdIndex.findRecordItemById(getValue(), getElement().getProject());
        Collection<PsiElement> elements = new LinkedList<>();
        items.forEach(def -> elements.add(def.getNavigationElement()));
        return PsiElementResolveResult.createResults(elements);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        List<OdooRecord> records = OdooExternalIdIndex.getAvailableRecords(getElement());
        List<LookupElement> elements = new LinkedList<>();
        records.forEach(record -> {
            if (myAcceptedModel == null || myAcceptedModel.equals(record.getModel())) {
                LookupElement element = LookupElementBuilder.create(record.getId())
                        .withTypeText(record.getModel())
                        .withIcon(PlatformIcons.XML_TAG_ICON);
                elements.add(element);
            }
        });
        return elements.toArray();
    }
}
