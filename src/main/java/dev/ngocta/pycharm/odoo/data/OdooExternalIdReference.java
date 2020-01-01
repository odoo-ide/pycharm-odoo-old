package dev.ngocta.pycharm.odoo.data;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.*;
import com.intellij.util.PlatformIcons;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class OdooExternalIdReference extends PsiReferenceBase<PsiElement> implements PsiPolyVariantReference {
    private final String myModel;
    private final OdooRecordSubType mySubType;
    private final boolean myAllowRelative;

    public OdooExternalIdReference(@NotNull PsiElement element,
                                   @Nullable String model,
                                   @Nullable OdooRecordSubType subType,
                                   boolean allowRelative) {
        super(element);
        myModel = model;
        mySubType = subType;
        myAllowRelative = allowRelative;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        String id = getValue();
        if (!id.contains(".") && myAllowRelative) {
            PsiDirectory module = OdooUtils.getOdooModule(getElement());
            if (module != null) {
                id = module.getName() + "." + id;
            }
        }
        Collection<OdooNavigableRecord> items = OdooExternalIdIndex.findNavigableRecordById(id, getElement().getProject());
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
        String moduleName = Optional.ofNullable(OdooUtils.getOdooModule(getElement()))
                .map(PsiDirectory::getName).orElse(null);
        records.forEach(record -> {
            if ((myModel == null || myModel.equals(record.getModel()))
                    && (mySubType == null || mySubType == record.getSubType())) {
                String id = record.getId();
                if (myAllowRelative && moduleName != null) {
                    String[] splits = id.split("\\.");
                    if (splits.length > 1 && splits[0].equals(moduleName)) {
                        id = splits[1];
                    }
                }
                LookupElement element = LookupElementBuilder.create(id)
                        .withTypeText(record.getModel())
                        .withIcon(PlatformIcons.XML_TAG_ICON);
                elements.add(element);
            }
        });
        return elements.toArray();
    }
}
