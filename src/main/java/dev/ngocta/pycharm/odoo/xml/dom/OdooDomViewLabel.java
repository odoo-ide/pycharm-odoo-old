package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;

public interface OdooDomViewLabel extends OdooDomModelScopedViewElement {
    @Attribute("for")
    @Referencing(OdooFieldNameReferenceConverter.class)
    GenericAttributeValue<String> getForAttribute();
}
