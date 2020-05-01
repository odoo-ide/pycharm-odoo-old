package dev.ngocta.pycharm.odoo.data;

import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.highlighting.BasicDomElementsInspection;
import com.intellij.util.xml.highlighting.DomElementAnnotationHolder;
import com.intellij.util.xml.highlighting.DomHighlightingHelper;

public class OdooXmlInspection extends BasicDomElementsInspection<OdooDomRoot> {
    public OdooXmlInspection() {
        super(OdooDomRoot.class);
    }

    @Override
    protected void checkDomElement(DomElement element,
                                   DomElementAnnotationHolder holder,
                                   DomHighlightingHelper helper) {
        super.checkDomElement(element, holder, helper);
        if (element instanceof OdooDomViewInheritLocator) {
            if (((OdooDomViewInheritLocator) element).getInheritedElement() == null) {
                holder.createProblem(element, HighlightSeverity.ERROR, "Can not find inherited element");
            }
        }
    }
}
