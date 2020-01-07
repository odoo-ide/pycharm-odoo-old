package dev.ngocta.pycharm.odoo.data;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.util.PlatformIcons;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        PsiElement element = getElement();
        String id = getValue();
        if (!id.contains(".") && myAllowRelative) {
            PsiDirectory module = OdooUtils.getOdooModule(element);
            if (module != null) {
                id = module.getName() + "." + id;
            }
        }
        Project project = element.getProject();
        List<OdooRecord> records = OdooExternalIdIndex.findRecordsById(id, project, element);
        List<PsiElement> elements = records.stream()
                .flatMap(item -> item.getElements(project).stream())
                .collect(Collectors.toList());
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
                if (myAllowRelative && record.getModule().equals(moduleName)) {
                    id = record.getName();
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
