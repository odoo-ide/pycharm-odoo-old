package dev.ngocta.pycharm.odoo.csv;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdReference;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordModelFilter;
import dev.ngocta.pycharm.odoo.python.model.OdooFieldInfo;
import dev.ngocta.pycharm.odoo.python.model.OdooFieldReference;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClass;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfo;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfoMap;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvTypes;
import net.seesharpsoft.intellij.plugins.csv.psi.impl.CsvFieldImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooCsvField extends CsvFieldImpl {
    public OdooCsvField(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        PsiFile file = getContainingFile();
        if (file instanceof CsvFile) {
            CsvFile csvFile = (CsvFile) file;
            String fileName = csvFile.getName();
            String modelName = null;
            if (fileName.toLowerCase().endsWith(".csv")) {
                modelName = fileName.substring(0, fileName.length() - 4);
            }
            if (modelName != null && !modelName.isEmpty()) {
                OdooModelClass modelClass = OdooModelClass.getInstance(modelName, getProject());
                CsvColumnInfoMap<PsiElement> columnInfoMap = csvFile.getColumnInfoMap();
                CsvColumnInfo<PsiElement> columnInfo = columnInfoMap.getColumnInfo(this);
                PsiElement header = columnInfo.getHeaderElement();
                if (header instanceof OdooCsvField) {
                    PsiElement textElement = ((OdooCsvField) header).getUnquotedElement();
                    if (textElement != null) {
                        String headerText = textElement.getText();
                        if (header == this) {
                            TextRange range = textElement.getTextRangeInParent();
                            if (headerText.endsWith(":id")) {
                                range = TextRange.create(range.getStartOffset(), range.getEndOffset() - 3);
                            }
                            return new OdooFieldReference(this, range, modelClass, null, null);
                        } else if (headerText.endsWith(":id")) {
                            String fieldName = headerText.substring(0, headerText.length() - 3);
                            PsiElement field = modelClass.findField(fieldName, TypeEvalContext.codeAnalysis(getProject(), csvFile));
                            OdooFieldInfo fieldInfo = OdooFieldInfo.getInfo(field);
                            if (fieldInfo != null) {
                                String comodel = fieldInfo.getComodel();
                                if (comodel != null) {
                                    return new OdooExternalIdReference(
                                            this, this.getUnquotedTextRange(), new OdooRecordModelFilter(comodel), true);
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.getReference();
    }

    @Nullable
    private TextRange getUnquotedTextRange() {
        PsiElement unquotedElement = getUnquotedElement();
        return unquotedElement != null ? unquotedElement.getTextRangeInParent() : null;
    }

    @Nullable
    private PsiElement getUnquotedElement() {
        ASTNode node = getNode().findChildByType(CsvTypes.TEXT);
        if (node != null) {
            return node.getPsi();
        }
        return null;
    }
}
