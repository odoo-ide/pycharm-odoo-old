package dev.ngocta.pycharm.odoo.xml.xpath;

import com.intellij.lang.Language;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiLanguageInjectionHost;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import org.intellij.lang.xpath.XPathLanguage;
import org.jetbrains.annotations.NotNull;

public class OdooXPathLanguageInjector implements LanguageInjector {
    public static final Language XPATH_LANG = Language.findInstance(XPathLanguage.class);
    public static final XmlAttributeValuePattern XPATH_PATTERN =
            XmlPatterns.xmlAttributeValue().withParent(
                    XmlPatterns.xmlAttribute("expr").withParent(
                            XmlPatterns.xmlTag().withLocalName("xpath"))).with(
                    OdooXmlUtils.ODOO_XML_DATA_ELEMENT_PATTERN_CONDITION);

    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host,
                                     @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
        if (XPATH_LANG != null && XPATH_PATTERN.accepts(host)) {
            TextRange textRange = ElementManipulators.getValueTextRange(host);
            injectionPlacesRegistrar.addPlace(XPATH_LANG, textRange, null, null);
        }
    }
}
