package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.*;

@Referencing(OdooFieldValueReferenceConverter.class)
public interface OdooDomFieldAssignment extends OdooDomField, GenericDomValue<String> {
    @Attribute("ref")
    @Referencing(OdooFieldValueReferenceConverter.class)
    GenericAttributeValue<String> getRef();

    default String getModel() {
        DomElement parent = getParent();
        if (parent instanceof OdooDomRecord) {
            return ((OdooDomRecord) parent).getModel().getStringValue();
        }
        return null;
    }
}
