package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.*;
import com.jetbrains.python.psi.PyUtil;
import org.intellij.plugins.xpathView.support.XPathSupport;
import org.jaxen.JaxenException;
import org.jaxen.XPath;

import java.util.List;

public interface OdooDomViewInheritLocator extends OdooDomViewElement {
    @Attribute("position")
    @Required
    GenericAttributeValue<String> getPosition();

    default String getXPathExpr() {
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
            OdooDomRecordLike domRecord = getParentOfType(OdooDomRecordLike.class, true);
            if (domRecord == null) {
                return null;
            }
            String inheritId = OdooDataUtils.getViewInheritId(domRecord);
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
                        String xpathExpr = support.getUniquePath(xmlTag, xmlTag);
                        DomElement domElement = DomManager.getDomManager(project).getDomElement(xmlTag);
                        if (domElement instanceof OdooDomFieldAssignment) {
                            xpathExpr += "/field[@name='arch']";
                        }
                        String innerXPathExpr = getXPathExpr();
                        if (!innerXPathExpr.startsWith("/")) {
                            xpathExpr += "/";
                        }
                        xpathExpr += innerXPathExpr;
                        XPath xpath = support.createXPath((XmlFile) file, xpathExpr);
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
