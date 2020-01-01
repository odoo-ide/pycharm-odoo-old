package dev.ngocta.pycharm.odoo.data;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import dev.ngocta.pycharm.odoo.model.OdooFieldReference;
import dev.ngocta.pycharm.odoo.model.OdooModelClass;
import org.jetbrains.annotations.NotNull;

public class OdooFieldNameReferenceConverter implements CustomReferenceConverter<String> {
    @NotNull
    @Override
    public PsiReference[] createReferences(GenericDomValue<String> value, PsiElement element, ConvertContext context) {
        OdooDomRecord record = value.getParentOfType(OdooDomRecord.class, true);
        if (record != null && record.getModel() != null && !record.getModel().isEmpty()) {
            String model = record.getModel();
            if (model != null && !model.isEmpty()) {
                return new PsiReference[]{new OdooFieldReference(element, OdooModelClass.create(model, element.getProject()))};
            }
        }
        return new PsiReference[0];
    }
}
