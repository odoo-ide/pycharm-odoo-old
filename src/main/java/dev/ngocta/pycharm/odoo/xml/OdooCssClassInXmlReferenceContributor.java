package dev.ngocta.pycharm.odoo.xml;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

public class OdooCssClassInXmlReferenceContributor extends PsiReferenceContributor {
    public static final XmlAttributeValuePattern CLASS_PATTERN =
            XmlPatterns.xmlAttributeValue("class", "icon")
                    .with(OdooXmlUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        OdooCssClassInXmlReferenceProvider provider = new OdooCssClassInXmlReferenceProvider();
        registrar.registerReferenceProvider(CLASS_PATTERN, provider);
    }
}
