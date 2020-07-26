package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.ObjectUtils;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdReference;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.data.OdooRecordViewInfo;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilter;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordModelFilter;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordViewModelFilter;
import org.jetbrains.annotations.NotNull;

public class OdooFieldRefValueReferenceConverter implements CustomReferenceConverter<String> {
    @NotNull
    @Override
    public PsiReference[] createReferences(GenericDomValue<String> value,
                                           PsiElement element,
                                           ConvertContext context) {
        OdooRecordFilter filter = null;
        DomElement parent = value.getParent();
        if (parent instanceof OdooDomFieldAssignment) {
            String model = ((OdooDomFieldAssignment) parent).getComodel();
            OdooDomFieldAssignment field = (OdooDomFieldAssignment) parent;
            if ("inherit_id".equals(field.getName())) {
                parent = parent.getParent();
                if (parent instanceof OdooDomRecord) {
                    OdooRecord record = ((OdooDomRecord) parent).getRecord();
                    if (record != null) {
                        OdooRecordViewInfo viewInfo = ObjectUtils.tryCast(record.getExtraInfo(), OdooRecordViewInfo.class);
                        if (viewInfo != null && viewInfo.getViewModel() != null) {
                            filter = new OdooRecordViewModelFilter(viewInfo.getViewModel(), record.getQualifiedId());
                        }
                    }
                }
            } else {
                filter = new OdooRecordModelFilter(model);
            }
        }
        return new PsiReference[]{
                new OdooExternalIdReference(element, null, filter, true)
        };
    }
}
