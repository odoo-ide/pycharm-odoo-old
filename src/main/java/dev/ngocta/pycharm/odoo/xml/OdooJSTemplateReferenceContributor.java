package dev.ngocta.pycharm.odoo.xml;

import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

public class OdooJSTemplateReferenceContributor extends PsiReferenceContributor {
    public static final XmlAttributeValuePattern T_CALL_PATTERN =
            XmlPatterns.xmlAttributeValue("t-call")
                    .with(OdooXmlUtils.ODOO_JS_TEMPLATE_ELEMENT_PATTERN_CONDITION);

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        OdooJSTemplateReferenceProvider provider = new OdooJSTemplateReferenceProvider();
        registrar.registerReferenceProvider(T_CALL_PATTERN, provider);
    }
}
