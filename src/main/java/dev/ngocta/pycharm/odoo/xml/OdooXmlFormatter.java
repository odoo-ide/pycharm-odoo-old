package dev.ngocta.pycharm.odoo.xml;

import com.intellij.formatting.CustomFormattingModelBuilder;
import com.intellij.lang.ASTNode;
import com.intellij.lang.xml.XmlFormattingModelBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.FormattingDocumentModelImpl;
import com.intellij.psi.formatter.xml.XmlBlock;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;

public class OdooXmlFormatter extends XmlFormattingModelBuilder implements CustomFormattingModelBuilder {
    @Override
    public boolean isEngagedToFormat(PsiElement context) {
        return OdooModuleUtils.isInOdooModule(context);
    }

    @Override
    protected XmlBlock createBlock(CodeStyleSettings settings,
                                   ASTNode root,
                                   FormattingDocumentModelImpl documentModel) {
        return new XmlBlock(root, null, null, new OdooXmlPolicy(settings, documentModel), null, null, false);
    }
}
