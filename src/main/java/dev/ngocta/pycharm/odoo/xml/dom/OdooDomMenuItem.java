package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdReference;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilters;
import org.jetbrains.annotations.NotNull;

public interface OdooDomMenuItem extends OdooDomRecordLike {
    @Attribute("name")
    GenericAttributeValue<String> getNameAttribute();

    @Attribute("groups")
    @Referencing(OdooGroupsReferenceConverter.class)
    GenericAttributeValue<String> getGroupsAttribute();

    @Attribute("parent")
    @Referencing(ParentReferenceConverter.class)
    GenericAttributeValue<String> getParentIdAttribute();

    @Attribute("action")
    @Referencing(ActionReferenceConverter.class)
    GenericAttributeValue<String> getActionAttribute();

    @Override
    default String getModel() {
        return OdooNames.IR_UI_MENU;
    }

    class ParentReferenceConverter implements CustomReferenceConverter<String> {
        @Override
        public PsiReference @NotNull [] createReferences(GenericDomValue<String> value,
                                                         PsiElement element,
                                                         ConvertContext context) {
            return new PsiReference[]{
                    new OdooExternalIdReference(element, null, OdooRecordFilters.IR_UI_MENU, true)
            };
        }
    }

    class ActionReferenceConverter implements CustomReferenceConverter<String> {
        @Override
        public PsiReference @NotNull [] createReferences(GenericDomValue<String> value,
                                                         PsiElement element,
                                                         ConvertContext context) {
            return new PsiReference[]{
                    new OdooExternalIdReference(element, null, OdooRecordFilters.ACTION_MODELS, true)
            };
        }
    }
}