package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.Nullable;

public interface OdooDomViewElement extends OdooDomElement {
    @Attribute("groups")
    @Referencing(OdooGroupsQualifiedReferenceConverter.class)
    GenericAttributeValue<String> getGroupsAttribute();

    @Nullable
    default String getViewType() {
        DomElement parent = getParent();
        if (parent instanceof OdooDomTemplate) {
            return OdooNames.VIEW_TYPE_QWEB;
        }
        if (parent instanceof OdooDomField) {
            String tagName = getXmlElementName();
            if ("t".equals(tagName) && parent instanceof OdooDomFieldAssignment) {
                return OdooNames.VIEW_TYPE_QWEB;
            }
            return tagName;
        }
        if (parent instanceof OdooDomViewElement) {
            return ((OdooDomViewElement) parent).getViewType();
        }
        return null;
    }
}
