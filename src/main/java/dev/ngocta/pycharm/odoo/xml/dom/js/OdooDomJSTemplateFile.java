package dev.ngocta.pycharm.odoo.xml.dom.js;

import com.intellij.util.xml.DomUtil;

import java.util.List;

public interface OdooDomJSTemplateFile extends OdooDomJSTemplateElement {
    default List<OdooDomJSTemplate> getAllTemplates() {
        return DomUtil.getChildrenOf(this, OdooDomJSTemplate.class);
    }
}
