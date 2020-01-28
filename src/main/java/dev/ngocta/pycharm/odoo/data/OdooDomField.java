package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import com.intellij.util.xml.Required;

public interface OdooDomField extends OdooDomElement, OdooDomModelScoped {
    @Attribute("name")
    @Required
    @Referencing(OdooFieldNameReferenceConverter.class)
    GenericAttributeValue<String> getName();
}
