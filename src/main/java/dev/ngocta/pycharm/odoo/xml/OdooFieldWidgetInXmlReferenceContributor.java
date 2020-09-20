package dev.ngocta.pycharm.odoo.xml;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import dev.ngocta.pycharm.odoo.javascript.OdooJSFieldWidgetReferenceProvider;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomViewField;
import org.jetbrains.annotations.NotNull;

public class OdooFieldWidgetInXmlReferenceContributor extends PsiReferenceContributor {
    public static final XmlAttributeValuePattern XML_FIELD_WIDGET_ATTR_PATTERN =
            XmlPatterns.xmlAttributeValue("widget").with(new PatternCondition<XmlAttributeValue>("") {
                @Override
                public boolean accepts(@NotNull XmlAttributeValue xmlAttributeValue, ProcessingContext context) {
                    DomElement domElement = DomUtil.getDomElement(xmlAttributeValue);
                    return domElement instanceof OdooDomViewField;
                }
            });

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        PsiReferenceProvider provider = new OdooJSFieldWidgetReferenceProvider();
        registrar.registerReferenceProvider(XML_FIELD_WIDGET_ATTR_PATTERN, provider);
    }
}
