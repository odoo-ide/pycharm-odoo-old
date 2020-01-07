package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.*;

import java.util.List;

public interface OdooDomRecord extends OdooDomRecordLike {
    @Attribute("model")
    @Required
    @Referencing(OdooModelReferenceConverter.class)
    GenericAttributeValue<String> getModel();

    @SubTag("field")
    List<OdooDomField> getFields();

    @Override
    default OdooRecord getRecord() {
        return getRecord(getModel().getStringValue(), null);
    }
}
