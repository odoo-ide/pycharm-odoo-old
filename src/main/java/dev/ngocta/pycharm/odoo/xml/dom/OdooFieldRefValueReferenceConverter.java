package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.ObjectUtils;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdReference;
import dev.ngocta.pycharm.odoo.data.OdooRecordViewInfo;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilter;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordModelFilter;
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
            parent = parent.getParent();
            if (parent instanceof OdooDomRecord) {
                OdooDomRecord record = (OdooDomRecord) parent;
                OdooDomFieldAssignment modelField = record.findField("model");
                if (modelField != null) {
                    String viewModel = modelField.getStringValue();
                    if (viewModel != null) {
                        filter = r -> {
                            OdooRecordViewInfo viewInfo = ObjectUtils.tryCast(r.getExtraInfo(), OdooRecordViewInfo.class);
                            return viewInfo != null && viewModel.equals(viewInfo.getViewModel());
                        };
                    } else {
                        filter = new OdooRecordModelFilter(model);
                    }
                }
            }
        }
        return new PsiReference[]{
                new OdooExternalIdReference(element, null, filter, true)
        };
    }
}
