package dev.ngocta.pycharm.odoo.data;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import dev.ngocta.pycharm.odoo.model.OdooFieldReference;
import dev.ngocta.pycharm.odoo.model.OdooModelClass;
import org.jetbrains.annotations.NotNull;

public class OdooFieldNameReferenceConverter implements CustomReferenceConverter<String> {
    @NotNull
    @Override
    public PsiReference[] createReferences(GenericDomValue<String> value,
                                           PsiElement element,
                                           ConvertContext context) {
        DomElement parent = value.getParent();
        if (parent instanceof OdooDomField) {
            String model = ((OdooDomField) parent).getModel();
            if (model != null && !model.isEmpty()) {
                return new PsiReference[]{new OdooFieldReference(element, () -> {
                    return OdooModelClass.getInstance(model, element.getProject());
                })};
            }
        }
        return new PsiReference[0];
    }
}
