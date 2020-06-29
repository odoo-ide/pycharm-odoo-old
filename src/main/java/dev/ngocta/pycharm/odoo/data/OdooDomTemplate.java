package dev.ngocta.pycharm.odoo.data;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

public interface OdooDomTemplate extends OdooDomRecordLike {
    @Override
    default OdooRecord getRecord() {
        return getRecord(OdooNames.IR_UI_VIEW, OdooRecordSubType.QWEB);
    }

    @Attribute("inherit_id")
    @Referencing(TemplateIdReferenceConverter.class)
    GenericAttributeValue<String> getInheritId();

    class TemplateIdReferenceConverter implements CustomReferenceConverter<String> {
        @NotNull
        @Override
        public PsiReference[] createReferences(GenericDomValue value,
                                               PsiElement element,
                                               ConvertContext context) {
            return new PsiReference[]{
                    new OdooExternalIdReference(element, null, OdooNames.IR_UI_VIEW, OdooRecordSubType.QWEB, true)
            };
        }
    }
}
