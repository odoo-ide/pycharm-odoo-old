package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdReference;
import dev.ngocta.pycharm.odoo.data.OdooRecordExtraInfo;
import dev.ngocta.pycharm.odoo.data.OdooRecordViewInfo;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilters;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OdooDomTemplate extends OdooDomRecordLike {
    @Attribute("inherit_id")
    @Referencing(TemplateIdReferenceConverter.class)
    GenericAttributeValue<String> getInheritIdAttribute();

    @Override
    default String getModel() {
        return OdooNames.IR_UI_VIEW;
    }

    default String getInheritId() {
        return getInheritIdAttribute().getStringValue();
    }

    default String getViewType() {
        return OdooNames.VIEW_TYPE_QWEB;
    }

    @Override
    @Nullable
    default OdooRecordExtraInfo getRecordExtraInfo() {
        return new OdooRecordViewInfo(getViewType(), null, getInheritId());
    }

    class TemplateIdReferenceConverter implements CustomReferenceConverter<String> {
        @NotNull
        @Override
        public PsiReference[] createReferences(GenericDomValue value,
                                               PsiElement element,
                                               ConvertContext context) {
            return new PsiReference[]{
                    new OdooExternalIdReference(element, null, OdooRecordFilters.QWEB, true)
            };
        }
    }
}
