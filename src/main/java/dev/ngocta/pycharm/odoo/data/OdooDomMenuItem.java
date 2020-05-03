package dev.ngocta.pycharm.odoo.data;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

public interface OdooDomMenuItem extends OdooDomRecordLike {
    @Override
    default OdooRecord getRecord() {
        return getRecord(OdooNames.IR_UI_MENU, null);
    }

    @Attribute("name")
    GenericAttributeValue<String> getName();

    @Attribute("groups")
    @Referencing(OdooGroupsReferenceConverter.class)
    GenericAttributeValue<String> getGroups();

    @Attribute("parent")
    @Referencing(ParentReferenceConverter.class)
    GenericAttributeValue<String> getParentId();

    @Attribute("action")
    @Referencing(ActionReferenceConverter.class)
    GenericAttributeValue<String> getAction();

    class ParentReferenceConverter implements CustomReferenceConverter<String> {
        @NotNull
        @Override
        public PsiReference[] createReferences(GenericDomValue<String> value,
                                               PsiElement element,
                                               ConvertContext context) {
            return new PsiReference[]{new OdooExternalIdReference(element, null,
                    () -> new String[]{OdooNames.IR_UI_MENU}, null, true)};
        }
    }

    class ActionReferenceConverter implements CustomReferenceConverter<String> {
        @NotNull
        @Override
        public PsiReference[] createReferences(GenericDomValue<String> value,
                                               PsiElement element,
                                               ConvertContext context) {
            return new PsiReference[]{new OdooExternalIdReference(element, null,
                    () -> OdooNames.ACTION_MODELS, null, true)};
        }
    }
}