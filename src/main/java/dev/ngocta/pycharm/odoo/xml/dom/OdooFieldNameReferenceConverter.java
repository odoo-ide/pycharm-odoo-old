package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import dev.ngocta.pycharm.odoo.python.model.OdooFieldReference;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClass;
import org.jetbrains.annotations.NotNull;

public class OdooFieldNameReferenceConverter implements CustomReferenceConverter<String> {
    @NotNull
    @Override
    public PsiReference[] createReferences(GenericDomValue<String> value,
                                           PsiElement element,
                                           ConvertContext context) {
        DomElement parent = value.getParent();
        OdooModelClass modelClass = null;
        if (parent instanceof OdooDomModelScoped) {
            String model = ((OdooDomModelScoped) parent).getModel();
            if (model != null && !model.isEmpty()) {
                modelClass = OdooModelClass.getInstance(model, element.getProject());
            }
        }
        return new PsiReference[]{new OdooFieldReference(element, modelClass)};
    }
}
