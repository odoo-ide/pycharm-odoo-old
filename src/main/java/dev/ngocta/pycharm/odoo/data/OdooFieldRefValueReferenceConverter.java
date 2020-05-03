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
        return new PsiReference[]{new OdooExternalIdReference(element, null,
                () -> {
                    DomElement parent = value.getParent();
                    if (parent instanceof OdooDomField) {
                        String comodel = ((OdooDomField) parent).getComodel();
                        if (comodel != null) {
                            return new String[]{comodel};
                        }
                    }
                    return null;
                }, null, true)};
    }
}
