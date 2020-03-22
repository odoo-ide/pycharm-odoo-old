package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.*;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.PythonLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdooPythonLanguageInjector implements LanguageInjector {
    private static final Pattern RE_PATTERN_PY = Pattern.compile("\\s*(.+)\\s*", Pattern.DOTALL);
    private static final Pattern RE_PATTERN_PY_TEMPLATE = Pattern.compile("(?:#\\{\\s*(.+?)\\s*})|(?:\\{\\{\\s*(.+?)\\s*}})", Pattern.DOTALL);

    public static final ElementPattern<String> XML_ATTRIBUTE_NAME_PY_PATTERN =
            StandardPatterns.or(
                    StandardPatterns.string().startsWith("t-att-"),
                    StandardPatterns.string().oneOf("eval", "attrs", "context", "options", "domain", "filter_domain",
                            "t-if", "t-elif", "t-foreach", "t-value", "t-esc", "t-raw", "t-field", "t-options"));

    public static final XmlAttributeValuePattern XML_ATTRIBUTE_VALUE_PY_PATTERN =
            XmlPatterns.xmlAttributeValue().withLocalName(XML_ATTRIBUTE_NAME_PY_PATTERN)
                    .with(OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    public static final XmlElementPattern.XmlTextPattern XML_TEXT_PY_PATTERN =
            XmlPatterns.xmlText().withParent(
                    XmlPatterns.xmlTag().withLocalName("attribute")
                            .with(new PatternCondition<XmlTag>("withAttributeValues") {
                                @Override
                                public boolean accepts(@NotNull final XmlTag xmlTag, final ProcessingContext context) {
                                    String name = xmlTag.getAttributeValue("name");
                                    return XML_ATTRIBUTE_NAME_PY_PATTERN.accepts(name);
                                }
                            })
            ).with(OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    public static final XmlAttributeValuePattern PY_TEMPLATE_PATTERN =
            XmlPatterns.xmlAttributeValue().withLocalName(StandardPatterns.string().startsWith("t-attf-"))
                    .with(OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host, @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
        if (XML_ATTRIBUTE_VALUE_PY_PATTERN.accepts(host) || XML_TEXT_PY_PATTERN.accepts(host)) {
            TextRange range = ElementManipulators.getValueTextRange(host);
            String text = ElementManipulators.getValueText(host);
            Matcher matcher = RE_PATTERN_PY.matcher(text);
            if (matcher.find()) {
                TextRange subRange = range.cutOut(new TextRange(matcher.start(1), matcher.end(1)));
                injectionPlacesRegistrar.addPlace(PythonLanguage.getInstance(), subRange, null, null);
            }
        } else if (PY_TEMPLATE_PATTERN.accepts(host)) {
            TextRange range = ElementManipulators.getValueTextRange(host);
            String text = ElementManipulators.getValueText(host);
            Matcher matcher = RE_PATTERN_PY_TEMPLATE.matcher(text);
            while (matcher.find()) {
                for (int i = 1; i <= matcher.groupCount(); i++) {
                    if (matcher.group(i) != null) {
                        TextRange subRange = range.cutOut(new TextRange(matcher.start(i), matcher.end(i)));
                        injectionPlacesRegistrar.addPlace(PythonLanguage.getInstance(), subRange, null, null);
                        break;
                    }
                }
            }
        }
    }
}
