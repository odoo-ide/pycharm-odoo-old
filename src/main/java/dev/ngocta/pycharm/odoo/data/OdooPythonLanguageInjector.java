package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.StandardPatterns;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.jetbrains.python.PythonLanguage;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdooPythonLanguageInjector implements LanguageInjector {
    private static final Pattern RE_PATTERN_PY = Pattern.compile("\\s*(.+)\\s*", Pattern.DOTALL);
    private static final Pattern RE_PATTERN_PY_TEMPLATE = Pattern.compile("(?:#\\{\\s*(.+?)\\s*})|(?:\\{\\{\\s*(.+?)\\s*}})", Pattern.DOTALL);

    public static final XmlAttributeValuePattern PY_PATTERN =
            XmlPatterns.xmlAttributeValue().withLocalName(StandardPatterns.or(
                    StandardPatterns.string().startsWith("t-att-"),
                    StandardPatterns.string().oneOf(
                            "eval", "attrs", "context", "options",
                            "t-if", "t-elif", "t-foreach", "t-value", "t-esc", "t-raw", "t-field", "t-options"))
            ).with(OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    public static final XmlAttributeValuePattern PY_TEMPLATE_PATTERN =
            XmlPatterns.xmlAttributeValue().withLocalName(StandardPatterns.string().startsWith("t-attf-"))
                    .with(OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host, @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
        if (PY_PATTERN.accepts(host)) {
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
