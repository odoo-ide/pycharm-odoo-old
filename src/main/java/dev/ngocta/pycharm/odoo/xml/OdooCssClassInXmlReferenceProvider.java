package dev.ngocta.pycharm.odoo.xml;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.css.util.CssResolveUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import dev.ngocta.pycharm.odoo.css.OdooCssClassReference;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class OdooCssClassInXmlReferenceProvider extends PsiReferenceProvider {

    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                           @NotNull ProcessingContext context) {
        List<PsiReference> references = new LinkedList<>();
        if (element instanceof XmlAttributeValue) {
            String value = ((XmlAttributeValue) element).getValue();
            TextRange baseRange = ElementManipulators.getValueTextRange(element);
            CssResolveUtil.consumeClassNames(value, element, (name, range) -> {
                references.add(new OdooCssClassReference(element, range.shiftRight(baseRange.getStartOffset()), true));
            });
        }
        return references.toArray(PsiReference.EMPTY_ARRAY);
    }
}
