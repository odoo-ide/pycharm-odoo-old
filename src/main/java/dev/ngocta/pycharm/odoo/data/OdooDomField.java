package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.*;
import dev.ngocta.pycharm.odoo.OdooNames;

@Referencing(OdooFieldValueReferenceConverter.class)
public interface OdooDomField extends GenericDomValue<String> {
    @Attribute(OdooNames.XML_FIELD_ATTR_NAME)
    @Required
    @Referencing(OdooFieldNameReferenceConverter.class)
    GenericAttributeValue<String> getName();

    @Attribute(OdooNames.XML_FIELD_ATTR_REF)
    @Referencing(OdooFieldValueReferenceConverter.class)
    GenericAttributeValue<String> getRef();
}
