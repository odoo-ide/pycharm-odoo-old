package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ObjectUtils;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdIndex;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.xml.OdooJSTemplateIndex;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import org.intellij.plugins.xpathView.support.XPathSupport;
import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface OdooDomViewInheritLocator extends OdooDomElement {
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

    @Nullable
    default XmlTag getInheritedViewArch() {
        XmlElement element = getXmlElement();
        if (element == null) {
            return null;
        }
        PsiFile file = element.getContainingFile();
        if (OdooXmlUtils.isOdooXmlDataElement(file)) {
            OdooDomRecordLike domRecord = getParentOfType(OdooDomRecordLike.class, true);
            if (domRecord == null) {
                return null;
            }
            String inheritId = OdooXmlUtils.getViewInheritId(domRecord);
            if (inheritId == null) {
                return null;
            }
            List<OdooRecord> records = OdooExternalIdIndex.findRecordsById(inheritId, element, true);
            if (records.isEmpty()) {
                return null;
            }
            List<PsiElement> recordElements = records.get(0).getElements(element.getProject());
            if (recordElements.isEmpty()) {
                return null;
            }
            XmlTag inheritedView = ObjectUtils.tryCast(recordElements.get(0), XmlTag.class);
            if (inheritedView == null) {
                return null;
            }
            if ("record".equals(inheritedView.getLocalName())) {
                for (XmlTag subTag : inheritedView.getSubTags()) {
                    if ("arch".equals(subTag.getAttributeValue("name"))) {
                        return subTag;
                    }
                }
                return null;
            }
            return inheritedView;
        } else if (OdooXmlUtils.isOdooJSTemplateElement(file)) {
            OdooDomJSTemplate template = getParentOfType(OdooDomJSTemplate.class, true);
            if (template == null) {
                return null;
            }
            String inheritId = template.getInheritName();
            if (inheritId == null) {
                return null;
            }
            List<OdooDomJSTemplate> inheritedTemplates = OdooJSTemplateIndex.findTemplatesByName(inheritId, file, true);
            if (inheritedTemplates.isEmpty()) {
                return null;
            }
            return inheritedTemplates.get(0).getXmlTag();
        }
        return null;
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
            XmlTag inheritedViewArch = getInheritedViewArch();
            if (inheritedViewArch == null) {
                return null;
            }
            XmlFile inheritedViewFile = ObjectUtils.tryCast(inheritedViewArch.getContainingFile(), XmlFile.class);
            if (inheritedViewFile == null) {
                return null;
            }
            try {
                XPathSupport support = XPathSupport.getInstance();
                String fullXPathExpr = support.getUniquePath(inheritedViewArch, inheritedViewArch);
                if (!xPathExpr.startsWith("/")) {
                    fullXPathExpr += "/";
                }
                fullXPathExpr += xPathExpr;
                XPath xpath = support.createXPath(inheritedViewFile, fullXPathExpr);
                Object result = xpath.selectSingleNode(inheritedViewArch);
                if (result instanceof XmlTag) {
                    return (XmlTag) result;
                }
            } catch (JaxenException ignored) {
            }
            return null;
        });
    }
}
