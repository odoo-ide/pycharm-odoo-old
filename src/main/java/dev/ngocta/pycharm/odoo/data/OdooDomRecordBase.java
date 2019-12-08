package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;

public interface OdooDomRecordBase extends OdooDomRecord {
    @Attribute("model")
    GenericAttributeValue<String> getModelAttr();

    default String getModel() {
        return getModelAttr().getValue();
    }
}
