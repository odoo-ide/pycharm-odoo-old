package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;

public interface OdooDomGroupsRestriction {
    @Attribute("groups")
    @Referencing(OdooGroupsReferenceConverter.class)
    GenericAttributeValue<String> getGroups();
}
