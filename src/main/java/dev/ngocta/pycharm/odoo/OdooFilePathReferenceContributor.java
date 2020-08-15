package dev.ngocta.pycharm.odoo;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import org.jetbrains.annotations.NotNull;

public class OdooFilePathReferenceContributor extends PsiReferenceContributor {
    public static final XmlAttributeValuePattern SCRIPT_SRC_PATTERN =
            XmlPatterns.xmlAttributeValue().withParent(
                    XmlPatterns.xmlAttribute("src").withParent(
                            XmlPatterns.xmlTag().withLocalName("script"))).with(
                    OdooXmlUtils.ODOO_XML_DATA_ELEMENT_PATTERN_CONDITION);

    public static final XmlAttributeValuePattern LINK_HREF_PATTERN =
            XmlPatterns.xmlAttributeValue().withParent(
                    XmlPatterns.xmlAttribute("href").withParent(
                            XmlPatterns.xmlTag().withLocalName("link"))).with(
                    OdooXmlUtils.ODOO_XML_DATA_ELEMENT_PATTERN_CONDITION);

    public static final XmlAttributeValuePattern T_THUMBNAIL_PATTERN =
            XmlPatterns.xmlAttributeValue().withParent(
                    XmlPatterns.xmlAttribute("t-thumbnail").withParent(
                            XmlPatterns.xmlTag().withLocalName("t"))).with(
                    OdooXmlUtils.ODOO_XML_DATA_ELEMENT_PATTERN_CONDITION);

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        PsiReferenceProvider provider = new OdooFilePathReferenceProvider();
        registrar.registerReferenceProvider(SCRIPT_SRC_PATTERN, provider);
        registrar.registerReferenceProvider(LINK_HREF_PATTERN, provider);
        registrar.registerReferenceProvider(T_THUMBNAIL_PATTERN, provider);
    }
}
