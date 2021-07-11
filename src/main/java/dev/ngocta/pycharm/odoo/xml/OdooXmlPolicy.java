package dev.ngocta.pycharm.odoo.xml;

import com.intellij.formatting.FormattingDocumentModel;
import com.intellij.formatting.WrapType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlPolicy;
import com.intellij.psi.xml.XmlElementType;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.xml.DomUtil;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomViewElement;

public class OdooXmlPolicy extends XmlPolicy {
    public OdooXmlPolicy(CodeStyleSettings settings,
                         FormattingDocumentModel documentModel) {
        super(settings, documentModel);
    }

    @Override
    public WrapType getWrappingTypeForTagBegin(XmlTag tag) {
        if (DomUtil.findDomElement(tag, OdooDomViewElement.class) != null) {
            PsiElement prevSibling = tag.getPrevSibling();
            if (prevSibling instanceof XmlText) {
                PsiElement[] children = prevSibling.getChildren();
                if (children.length == 1 && children[0] instanceof PsiWhiteSpace) {
                    prevSibling = prevSibling.getPrevSibling();
                }
            }
            if (prevSibling.getNode().getElementType() != XmlElementType.XML_COMMENT) {
                return WrapType.NONE;
            }
        }
        return super.getWrappingTypeForTagBegin(tag);
    }

    @Override
    public WrapType getWrappingTypeForTagEnd(XmlTag xmlTag) {
        return WrapType.NONE;
    }

    @Override
    public boolean shouldSaveSpacesBetweenTagAndText() {
        return true;
    }
}
