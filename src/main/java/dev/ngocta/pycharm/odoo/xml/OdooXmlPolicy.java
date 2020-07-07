package dev.ngocta.pycharm.odoo.xml;

import com.intellij.formatting.FormattingDocumentModel;
import com.intellij.formatting.WrapType;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.xml.XmlPolicy;
import com.intellij.psi.xml.XmlTag;

public class OdooXmlPolicy extends XmlPolicy {
    public OdooXmlPolicy(CodeStyleSettings settings,
                         FormattingDocumentModel documentModel) {
        super(settings, documentModel);
    }

    @Override
    public WrapType getWrappingTypeForTagBegin(XmlTag tag) {
        return WrapType.NONE;
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
