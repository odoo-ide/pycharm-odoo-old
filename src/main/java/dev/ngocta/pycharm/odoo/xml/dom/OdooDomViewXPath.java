package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;

public interface OdooDomViewXPath extends OdooDomViewInheritLocator {
    @Attribute("expr")
    @Required
    GenericAttributeValue<String> getExpr();

    default String getXPathExpr() {
        return getExpr().getStringValue();
    }
}

