package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import org.jetbrains.annotations.Nullable;

public interface OdooDomViewElement extends OdooDomElement {
    @Attribute("groups")
    @Referencing(OdooGroupsReferenceConverter.class)
    GenericAttributeValue<String> getGroups();

    @Nullable
    default OdooDomViewType getViewType() {
        DomElement parent = getParent();
        if (parent instanceof OdooDomTemplate) {
            return OdooDomViewType.QWeb;
        }
        if (parent instanceof OdooDomField) {
            switch (getXmlElementName()) {
                case "form":
                    return OdooDomViewType.Form;
                case "tree":
                    return OdooDomViewType.Tree;
                case "search":
                    return OdooDomViewType.Search;
                case "kanban":
                    return OdooDomViewType.Kanban;
            }
        }
        if (parent instanceof OdooDomViewElement) {
            return ((OdooDomViewElement) parent).getViewType();
        }
        return null;
    }
}
