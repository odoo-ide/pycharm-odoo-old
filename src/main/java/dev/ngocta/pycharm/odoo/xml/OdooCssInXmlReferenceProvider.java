package dev.ngocta.pycharm.odoo.xml;

import com.intellij.psi.css.impl.util.CssInHtmlClassOrIdReferenceProvider;
import com.intellij.psi.xml.XmlAttribute;

public class OdooCssInXmlReferenceProvider extends CssInHtmlClassOrIdReferenceProvider {
    @Override
    protected boolean isSuitableAttribute(String attrName,
                                          XmlAttribute xmlAttribute) {
        return "class".equalsIgnoreCase(attrName);
    }
}
