package dev.ngocta.pycharm.odoo.xml;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

public class OdooCssInXmlReferenceContributor extends PsiReferenceContributor {
    public static final XmlAttributeValuePattern CLASS_PATTERN =
            XmlPatterns.xmlAttributeValue("class")
                    .with(OdooXmlUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        OdooCssInXmlReferenceProvider provider = new OdooCssInXmlReferenceProvider();
        registrar.registerReferenceProvider(CLASS_PATTERN, provider);
    }
}
