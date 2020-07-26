package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdIndex;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import org.intellij.plugins.xpathView.support.XPathSupport;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import java.util.List;

public interface OdooDomViewInheritLocator extends OdooDomViewElement {
    @Attribute("position")
    GenericAttributeValue<String> getPositionAttribute();

    default String getExpr() {
        StringBuilder xpath = new StringBuilder("//" + getXmlElementName());
        XmlTag tag = getXmlTag();
        if (tag != null) {
            for (XmlAttribute attr : tag.getAttributes()) {
                if (!"position".equals(attr.getName())) {
                    xpath.append(String.format("[@%s='%s']", attr.getName(), StringUtil.notNullize(attr.getValue())));
                }
            }
        }
        return xpath.toString();
    }

    default XmlTag getInheritedElement() {
        XmlElement element = getXmlElement();
        if (element == null) {
            return null;
        }
        try {
            Class.forName("org.intellij.plugins.xpathView.support.XPathSupport");
        } catch (NoClassDefFoundError | ClassNotFoundException ignored) {
            return null;
        }
        return PyUtil.getNullableParameterizedCachedValue(element, null, param -> {
            String xPathExpr = getExpr();
            if (xPathExpr == null) {
                return null;
            }
            OdooDomRecordLike domRecord = getParentOfType(OdooDomRecordLike.class, true);
            if (domRecord == null) {
                return null;
            }
            String inheritId = OdooXmlUtils.getViewInheritId(domRecord);
            if (inheritId == null) {
                return null;
            }
            Project project = element.getProject();
            List<OdooRecord> records = OdooExternalIdIndex.findRecordsById(inheritId, element);
            if (records.isEmpty()) {
                return null;
            }
            List<PsiElement> recordElements = records.get(0).getElements(project);
            if (recordElements.isEmpty()) {
                return null;
            }
            PsiElement recordElement = recordElements.get(0);
            if (recordElement instanceof XmlTag) {
                XmlTag xmlTag = (XmlTag) recordElement;
                PsiFile file = xmlTag.getContainingFile();
                if (file instanceof XmlFile) {
                    try {
                        XPathSupport support = XPathSupport.getInstance();
                        String fullXPathExpr = support.getUniquePath(xmlTag, xmlTag);
                        if ("record".equals(xmlTag.getLocalName())) {
                            fullXPathExpr += "/field[@name='arch']";
                        }
                        if (!xPathExpr.startsWith("/")) {
                            fullXPathExpr += "/";
                        }
                        fullXPathExpr += xPathExpr;
                        XPath xpath = support.createXPath((XmlFile) file, fullXPathExpr);
                        Object result = xpath.selectSingleNode(xmlTag);
                        if (result instanceof XmlTag) {
                            return (XmlTag) result;
                        }
                    } catch (JaxenException ignored) {
                    }
                }
            }
            return null;
        });
    }
}
