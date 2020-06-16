package dev.ngocta.pycharm.odoo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.*;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import dev.ngocta.pycharm.odoo.data.OdooDataUtils;
import dev.ngocta.pycharm.odoo.data.OdooDomFieldAssignment;
import dev.ngocta.pycharm.odoo.model.OdooModelUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdooLanguageInjector implements LanguageInjector {
    private static final Pattern RE_PATTERN_PY = Pattern.compile("\\s*(.*\\S)\\s*", Pattern.DOTALL);
    private static final Pattern RE_PATTERN_PY_TEMPLATE = Pattern.compile("(?:#\\{\\s*(.+?)\\s*})|(?:\\{\\{\\s*(.+?)\\s*}})", Pattern.DOTALL);
    private static final Map<String, Set<String>> KNOWN_FIELDS_TO_INJECT = ImmutableMap.<String, Set<String>>builder()
            .put(OdooNames.IR_RULE_DOMAIN_FORCE, ImmutableSet.of(OdooNames.IR_RULE))
            .put("domain", ImmutableSet.of(OdooNames.IR_ACTIONS_ACT_WINDOW))
            .put("context", ImmutableSet.of(OdooNames.IR_ACTIONS_ACT_WINDOW))
            .put("code", ImmutableSet.of(OdooNames.IR_ACTIONS_SERVER, OdooNames.IR_CRON))
            .build();

    public static final ElementPattern<String> XML_ATTRIBUTE_NAME_PATTERN =
            StandardPatterns.or(
                    StandardPatterns.string().startsWith("t-att-"),
                    StandardPatterns.string().oneOf("eval", "attrs", "context", "options", "domain", "filter_domain",
                            "t-if", "t-elif", "t-foreach", "t-value", "t-esc", "t-raw", "t-field", "t-options"));

    public static final XmlAttributeValuePattern XML_ATTRIBUTE_VALUE_PATTERN =
            XmlPatterns.xmlAttributeValue()
                    .withLocalName(XML_ATTRIBUTE_NAME_PATTERN)
                    .with(OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    public static final XmlElementPattern.XmlTextPattern XML_ATTRIBUTE_VALUE_OVERRIDE_PATTERN =
            XmlPatterns.xmlText().withParent(
                    XmlPatterns.xmlTag().withLocalName("attribute").with(
                            new PatternCondition<XmlTag>("attributeValue") {
                                @Override
                                public boolean accepts(@NotNull final XmlTag xmlTag,
                                                       final ProcessingContext context) {
                                    String name = xmlTag.getAttributeValue("name");
                                    return XML_ATTRIBUTE_NAME_PATTERN.accepts(name);
                                }
                            }
                    )
            ).with(OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    public static final XmlElementPattern.XmlTextPattern XML_TEXT_FIELD_VALUE_PATTERN =
            XmlPatterns.xmlText().with(new PatternCondition<XmlText>("fieldValue") {
                @Override
                public boolean accepts(@NotNull XmlText xmlText,
                                       ProcessingContext context) {
                    XmlTag tag = xmlText.getParentTag();
                    if (tag == null) {
                        return false;
                    }
                    Project project = tag.getProject();
                    DomManager domManager = DomManager.getDomManager(project);
                    DomElement domElement = domManager.getDomElement(tag);
                    if (domElement instanceof OdooDomFieldAssignment) {
                        OdooDomFieldAssignment fieldAssignment = (OdooDomFieldAssignment) domElement;
                        String field = fieldAssignment.getName().getStringValue();
                        String model = fieldAssignment.getModel();
                        if (field != null && model != null) {
                            return KNOWN_FIELDS_TO_INJECT.getOrDefault(field, Collections.emptySet()).contains(model);
                        }
                    }
                    return false;
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> RELATION_FIELD_DOMAIN_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(-1, OdooNames.FIELD_ATTR_DOMAIN, OdooNames.RELATIONAL_FIELD_TYPES);

    public static final XmlAttributeValuePattern PY_TEMPLATE_PATTERN =
            XmlPatterns.xmlAttributeValue()
                    .withLocalName(StandardPatterns.string().startsWith("t-attf-"))
                    .with(OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION);

    public static final XmlElementPattern.XmlTextPattern MAIL_TEMPLATE_BODY_PATTERN =
            XmlPatterns.xmlText().with(new PatternCondition<XmlText>("mailTemplateBody") {
                @Override
                public boolean accepts(@NotNull XmlText xmlText,
                                       ProcessingContext context) {
                    XmlTag tag = xmlText.getParentTag();
                    if (tag == null) {
                        return false;
                    }
                    Project project = xmlText.getProject();
                    DomManager domManager = DomManager.getDomManager(project);
                    DomElement domElement = domManager.getDomElement(tag);
                    if (domElement instanceof OdooDomFieldAssignment) {
                        OdooDomFieldAssignment fieldAssignment = (OdooDomFieldAssignment) domElement;
                        String field = fieldAssignment.getName().getStringValue();
                        String model = fieldAssignment.getModel();
                        return "body_html".equals(field) && OdooNames.MAIL_TEMPLATE.equals(model);
                    }
                    return false;
                }
            });

    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host,
                                     @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
        if (XML_ATTRIBUTE_VALUE_PATTERN.accepts(host)
                || XML_ATTRIBUTE_VALUE_OVERRIDE_PATTERN.accepts(host)
                || XML_TEXT_FIELD_VALUE_PATTERN.accepts(host)
                || RELATION_FIELD_DOMAIN_PATTERN.accepts(host)) {
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
        } else if (MAIL_TEMPLATE_BODY_PATTERN.accepts(host)) {
            TextRange range = ElementManipulators.getValueTextRange(host);
            injectionPlacesRegistrar.addPlace(HTMLLanguage.INSTANCE, range, null, null);
        }
    }
}
