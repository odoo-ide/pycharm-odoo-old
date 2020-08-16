package dev.ngocta.pycharm.odoo.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomElement;

public class OdooXmlInspection extends BasicDomElementsInspection<OdooDomElement> {
    public OdooXmlInspection() {
        super(OdooDomElement.class);
    }

    @Override
    protected void checkDomElement(DomElement element,
                                   DomElementAnnotationHolder holder,
                                   DomHighlightingHelper helper) {
        super.checkDomElement(element, holder, helper);
    }
}
