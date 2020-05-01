package dev.ngocta.pycharm.odoo.data;

import com.intellij.formatting.CustomFormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.xml.XmlFormattingModelBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.xml.XmlBlock;
import com.intellij.psi.xml.XmlFile;

public class OdooXmlFormatter extends XmlFormattingModelBuilder implements CustomFormattingModelBuilder {
    @Override
    public boolean isEngagedToFormat(PsiElement context) {
        PsiElement file = context.getContainingFile();
        if (file instanceof XmlFile) {
            return OdooDataUtils.getDomRoot((XmlFile) file) != null;
        }
        return false;
    }

    @Override
    protected XmlBlock createBlock(CodeStyleSettings settings,
                                   ASTNode root,
                                   FormattingDocumentModelImpl documentModel) {
        return new XmlBlock(root, null, null, new OdooXmlPolicy(settings, documentModel), null, null, false);
    }
}
