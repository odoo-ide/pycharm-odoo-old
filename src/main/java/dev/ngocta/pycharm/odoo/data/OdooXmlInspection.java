package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.highlighting.BasicDomElementsInspection;

public class OdooXmlInspection extends BasicDomElementsInspection<OdooDomRoot> {
    public OdooXmlInspection() {
        super(OdooDomRoot.class);
    }
}
