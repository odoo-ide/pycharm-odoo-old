package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;

public interface OdooDomAccessGroupsAware {
    @Attribute("groups")
    @Referencing(OdooGroupsReferenceConverter.class)
    GenericAttributeValue<String> getAccessGroups();
}
