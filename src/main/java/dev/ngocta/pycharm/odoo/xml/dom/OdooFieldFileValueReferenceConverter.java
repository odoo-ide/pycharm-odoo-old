package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import dev.ngocta.pycharm.odoo.OdooFileReferenceSet;
import org.jetbrains.annotations.NotNull;

public class OdooFieldFileValueReferenceConverter implements CustomReferenceConverter<String> {
    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<String> value,
                                                     PsiElement element,
                                                     ConvertContext context) {
        FileReferenceSet referenceSet = new OdooFileReferenceSet(element, true);
        return referenceSet.getAllReferences();
    }
}
