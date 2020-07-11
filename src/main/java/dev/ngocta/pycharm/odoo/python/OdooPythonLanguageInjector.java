package dev.ngocta.pycharm.odoo.python;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.*;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.jetbrains.python.PythonLanguage;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomFieldAssignment;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomViewElement;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomViewType;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdooPythonLanguageInjector implements LanguageInjector {
    private static final Pattern RE_PATTERN_PY = Pattern.compile("\\s*(.*\\S)\\s*", Pattern.DOTALL);
    private static final Pattern RE_PATTERN_PY_TEMPLATE = Pattern.compile("(?:#\\{\\s*(.+?)\\s*})|(?:\\{\\{\\s*(.+?)\\s*}})", Pattern.DOTALL);
    private static final Map<String, Set<String>> KNOWN_FIELDS_TO_INJECT = ImmutableMap.<String, Set<String>>builder()
            .put(OdooNames.IR_RULE_DOMAIN_FORCE, ImmutableSet.of(OdooNames.IR_RULE))
            .put("domain", ImmutableSet.of(OdooNames.IR_ACTIONS_ACT_WINDOW))
            .put("context", ImmutableSet.of(OdooNames.IR_ACTIONS_ACT_WINDOW))
            .put("code", ImmutableSet.of(OdooNames.IR_ACTIONS_SERVER, OdooNames.IR_CRON))
            .build();

    public static final XmlAttributeValuePattern XML_ATTRIBUTE_VALUE_PATTERN =
            XmlPatterns.xmlAttributeValue().with(new PatternCondition<XmlAttributeValue>("xmlAttributeValue") {
                @Override
                public boolean accepts(@NotNull XmlAttributeValue xmlAttributeValue,
                                       ProcessingContext context) {
                    String attributeName = XmlAttributeValuePattern.getLocalName(xmlAttributeValue);
                    if (attributeName == null) {
                        return false;
                    }
                    XmlTag tag = PsiTreeUtil.getParentOfType(xmlAttributeValue, XmlTag.class);
                    if (tag == null) {
                        return false;
                    }
                    DomElement domElement = DomManager.getDomManager(xmlAttributeValue.getProject()).getDomElement(tag);
                    if (domElement instanceof OdooDomFieldAssignment) {
                        return ArrayUtil.contains(attributeName, "eval");
                    }
                    if (domElement instanceof OdooDomViewElement) {
                        OdooDomViewElement domViewElement = (OdooDomViewElement) domElement;
                        OdooDomViewType type = domViewElement.getViewType();
                        if (OdooDomViewType.QWeb.equals(type)) {
                            if (attributeName.startsWith("t-att-")) {
                                return true;
                            }
                            return ArrayUtil.contains(attributeName,
                                    "t-if", "t-elif", "t-foreach", "t-set", "t-value",
                                    "t-as", "t-esc", "t-raw", "t-field", "t-options");
                        }
                        if ("tree".equals(tag.getLocalName())) {
                            return attributeName.startsWith("decoration-");
                        }
                        return ArrayUtil.contains(attributeName, "attrs", "context", "options", "domain", "filter_domain");
                    }
                    return false;
                }
            });

    public static final XmlElementPattern.XmlTextPattern XML_ATTRIBUTE_VALUE_INHERITANCE_PATTERN =
            XmlPatterns.xmlText().withParent(XmlPatterns.xmlTag().withLocalName("attribute").with(new PatternCondition<XmlTag>("xmlAttributeValueOverride") {
                @Override
                public boolean accepts(@NotNull final XmlTag xmlTag,
                                       final ProcessingContext context) {
                    String name = xmlTag.getAttributeValue("name");
                    if (name == null) {
                        return false;
                    }
                    DomElement domElement = DomManager.getDomManager(xmlTag.getProject()).getDomElement(xmlTag);
                    if (domElement instanceof OdooDomViewElement) {
                        return ArrayUtil.contains(name, "attrs", "context", "options", "domain", "filter_domain");
                    }
                    return false;
                }
            }));


    public static final XmlElementPattern.XmlTextPattern XML_TEXT_FIELD_VALUE_PATTERN =
            XmlPatterns.xmlText().with(new PatternCondition<XmlText>("xmlTextFieldValue") {
                @Override
                public boolean accepts(@NotNull XmlText xmlText,
                                       ProcessingContext context) {
                    XmlTag tag = xmlText.getParentTag();
                    if (tag == null) {
                        return false;
                    }
                    DomElement domElement = DomManager.getDomManager(tag.getProject()).getDomElement(tag);
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
            XmlPatterns.xmlAttributeValue().with(new PatternCondition<XmlAttributeValue>("pyTemplate") {
                @Override
                public boolean accepts(@NotNull XmlAttributeValue xmlAttributeValue,
                                       ProcessingContext context) {
                    String attributeName = XmlAttributeValuePattern.getLocalName(xmlAttributeValue);
                    if (attributeName != null && attributeName.startsWith("t-attf-")) {
                        XmlTag tag = PsiTreeUtil.getParentOfType(xmlAttributeValue, XmlTag.class);
                        if (tag == null) {
                            return false;
                        }
                        DomElement domElement = DomManager.getDomManager(xmlAttributeValue.getProject()).getDomElement(tag);
                        if (domElement instanceof OdooDomViewElement) {
                            OdooDomViewElement domViewElement = (OdooDomViewElement) domElement;
                            return OdooDomViewType.QWeb.equals(domViewElement.getViewType());
                        }
                    }
                    return false;
                }
            });

    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host,
                                     @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
        if (XML_ATTRIBUTE_VALUE_PATTERN.accepts(host)
                || XML_ATTRIBUTE_VALUE_INHERITANCE_PATTERN.accepts(host)
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
        }
    }
}
