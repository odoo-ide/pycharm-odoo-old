package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.*;

import java.util.List;

public interface OdooDomRecordBase extends OdooDomRecord {
    @Attribute("model")
    @Required
    @Referencing(OdooModelReferenceConverter.class)
    GenericAttributeValue<String> getModelAttr();

    default String getModel() {
        return getModelAttr().getValue();
    }

    @SubTag("field")
    List<OdooDomField> getFields();
}
