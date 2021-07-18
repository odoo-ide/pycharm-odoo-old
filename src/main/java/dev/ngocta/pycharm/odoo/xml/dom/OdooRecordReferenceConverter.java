package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.GenericDomValue;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdReference;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilter;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordModelFilter;
import org.jetbrains.annotations.NotNull;

public class OdooRecordReferenceConverter implements CustomReferenceConverter<String> {
    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<String> value,
                                                     PsiElement element,
                                                     ConvertContext context) {
        OdooDomRecordLike domRecordLike = DomUtil.findDomElement(element, OdooDomRecordLike.class);
        OdooRecordFilter filter = null;
        if (domRecordLike != null && !StringUtil.isEmpty(domRecordLike.getModel())) {
            filter = new OdooRecordModelFilter(domRecordLike.getModel());
        }
        return new PsiReference[]{new OdooExternalIdReference(element, null, filter, true)};
    }
}
