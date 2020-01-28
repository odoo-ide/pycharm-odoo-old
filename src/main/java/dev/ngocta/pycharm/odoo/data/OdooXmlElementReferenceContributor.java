package dev.ngocta.pycharm.odoo.data;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import dev.ngocta.pycharm.odoo.OdooFilePathReferenceProvider;
import org.jetbrains.annotations.NotNull;

public class OdooXmlElementReferenceContributor extends PsiReferenceContributor {
    public static final XmlAttributeValuePattern SCRIPT_SRC_PATTERN =
            XmlPatterns.xmlAttributeValue().withParent(
                    XmlPatterns.xmlAttribute("src").withParent(
                            XmlPatterns.xmlTag().withLocalName("script"))).with(
                    OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    public static final XmlAttributeValuePattern LINK_HREF_PATTERN =
            XmlPatterns.xmlAttributeValue().withParent(
                    XmlPatterns.xmlAttribute("href").withParent(
                            XmlPatterns.xmlTag().withLocalName("link"))).with(
                    OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(SCRIPT_SRC_PATTERN, new OdooFilePathReferenceProvider());
        registrar.registerReferenceProvider(LINK_HREF_PATTERN, new OdooFilePathReferenceProvider());
    }
}
