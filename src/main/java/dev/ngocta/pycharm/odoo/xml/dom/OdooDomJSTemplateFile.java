package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.DomUtil;

import java.util.List;

public interface OdooDomJSTemplateFile extends OdooDomElement {
    default List<OdooDomJSTemplate> getAllTemplates() {
        return DomUtil.getChildrenOf(this, OdooDomJSTemplate.class);
    }
}
