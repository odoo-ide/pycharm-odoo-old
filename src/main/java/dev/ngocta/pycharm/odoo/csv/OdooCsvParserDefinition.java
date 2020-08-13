package dev.ngocta.pycharm.odoo.csv;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import net.seesharpsoft.intellij.plugins.csv.CsvParserDefinition;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes;
import org.jetbrains.annotations.NotNull;

public class OdooCsvParserDefinition extends CsvParserDefinition {
    @Override
    @NotNull
    public PsiElement createElement(ASTNode node) {
        if (node.getElementType() == CsvTypes.FIELD) {
            return new OdooCsvField(node);
        }
        return super.createElement(node);
    }
}
