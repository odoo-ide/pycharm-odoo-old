package dev.ngocta.pycharm.odoo.xml;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;

public interface OdooRecordTag extends DomElement {
    @Attribute("id")
    GenericAttributeValue<String> getId();

    @Attribute("model")
    GenericAttributeValue<String> getModel();
}
