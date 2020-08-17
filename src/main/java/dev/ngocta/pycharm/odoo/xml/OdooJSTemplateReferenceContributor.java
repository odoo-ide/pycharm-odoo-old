package dev.ngocta.pycharm.odoo.xml;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class OdooJSTemplateReferenceContributor extends PsiReferenceContributor {
    public static final XmlAttributeValuePattern T_PATTERN =
            XmlPatterns.xmlAttributeValue("t-inherit", "t-extend", "t-call")
                    .with(OdooXmlUtils.ODOO_JS_TEMPLATE_ELEMENT_PATTERN_CONDITION)
                    .with(new PatternCondition<XmlAttributeValue>("") {
                        @Override
                        public boolean accepts(@NotNull XmlAttributeValue xmlAttributeValue,
                                               ProcessingContext context) {
                            String attr = XmlAttributeValuePattern.getLocalName(xmlAttributeValue);
                            if ("t-inherit".equals(attr)) {
                                context.put(OdooJSTemplateReferenceProvider.IS_QUALIFIED, true);
                            }
                            return true;
                        }
                    });

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        OdooJSTemplateReferenceProvider provider = new OdooJSTemplateReferenceProvider();
        registrar.registerReferenceProvider(T_PATTERN, provider);
    }
}
