package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.JavascriptLanguage;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;

public class OdooJSLanguageInjector implements LanguageInjector {
    public static final XmlAttributeValuePattern XML_ATTR_VALUE_PATTERN =
            XmlPatterns.xmlAttributeValue().with(new PatternCondition<XmlAttributeValue>("xmlAttributeValue") {
                @Override
                public boolean accepts(@NotNull XmlAttributeValue xmlAttributeValue,
                                       ProcessingContext context) {
                    String attributeName = XmlAttributeValuePattern.getLocalName(xmlAttributeValue);
                    if (attributeName == null) {
                        return false;
                    }
                    if (OdooXmlUtils.isOdooJSTemplateElement(xmlAttributeValue)) {
                        if (attributeName.startsWith("t-att-")) {
                            return true;
                        }
                        return ArrayUtil.contains(attributeName,
                                "t-if", "t-elif", "t-foreach", "t-set", "t-value", "t-as", "t-esc");
                    }
                    return false;
                }
            });

    public static final XmlAttributeValuePattern XML_ATTR_VALUE_FORMAT_STRING_PATTERN =
            XmlPatterns.xmlAttributeValue().with(new PatternCondition<XmlAttributeValue>("xmlAttributeValueFormatString") {
                @Override
                public boolean accepts(@NotNull XmlAttributeValue xmlAttributeValue,
                                       ProcessingContext context) {
                    String attributeName = XmlAttributeValuePattern.getLocalName(xmlAttributeValue);
                    if (attributeName != null && attributeName.startsWith("t-attf-")) {
                        return OdooXmlUtils.isOdooJSTemplateElement(xmlAttributeValue);
                    }
                    return false;
                }
            });

    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host,
                                     @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
        if (XML_ATTR_VALUE_PATTERN.accepts(host)) {
            TextRange range = ElementManipulators.getValueTextRange(host);
            String text = ElementManipulators.getValueText(host);
            Matcher matcher = OdooXmlUtils.XML_ATTR_VALUE_RE_PATTERN.matcher(text);
            if (matcher.find()) {
                TextRange subRange = range.cutOut(new TextRange(matcher.start(1), matcher.end(1)));
                injectionPlacesRegistrar.addPlace(JavascriptLanguage.INSTANCE, subRange, "var ___ = ", null);
            }
        } else if (XML_ATTR_VALUE_FORMAT_STRING_PATTERN.accepts(host)) {
            TextRange range = ElementManipulators.getValueTextRange(host);
            String text = ElementManipulators.getValueText(host);
            Matcher matcher = OdooXmlUtils.XML_ATTR_VALUE_RE_PATTERN_FORMAT_STRING.matcher(text);
            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    if (matcher.group(i) != null) {
                        TextRange subRange = range.cutOut(new TextRange(matcher.start(i), matcher.end(i)));
                        injectionPlacesRegistrar.addPlace(JavascriptLanguage.INSTANCE, subRange, "var ___ = ", null);
                        break;
                    }
                }
            }
        }
    }
}
