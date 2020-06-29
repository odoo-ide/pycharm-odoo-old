package dev.ngocta.pycharm.odoo.data;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericDomValue;
import org.jetbrains.annotations.NotNull;

public class OdooFieldRefValueReferenceConverter implements CustomReferenceConverter<String> {
    @NotNull
    @Override
    public PsiReference[] createReferences(GenericDomValue<String> value,
                                           PsiElement element,
                                           ConvertContext context) {
        String model = null;
        DomElement parent = value.getParent();
        if (parent instanceof OdooDomField) {
            model = ((OdooDomField) parent).getComodel();
        }
        return new PsiReference[]{
                new OdooExternalIdReference(element, null, model, null, true)
        };
    }
}
