package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import dev.ngocta.pycharm.odoo.python.model.OdooModelReference;
import org.jetbrains.annotations.NotNull;

public class OdooModelReferenceConverter implements CustomReferenceConverter<String> {
    @NotNull
    @Override
    public PsiReference[] createReferences(GenericDomValue<String> value,
                                           PsiElement element,
                                           ConvertContext context) {
        return new PsiReference[]{new OdooModelReference(element)};
    }
}
