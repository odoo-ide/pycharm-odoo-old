package dev.ngocta.pycharm.odoo.csv;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdReference;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilters;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfo;
import net.seesharpsoft.intellij.plugins.csv.CsvColumnInfoMap;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import net.seesharpsoft.intellij.plugins.csv.psi.impl.CsvFieldImpl;
import org.jetbrains.annotations.NotNull;

public class OdooCsvField extends CsvFieldImpl {
    public OdooCsvField(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PsiReference getReference() {
        PsiFile file = getContainingFile();
        if (file instanceof CsvFile && file.getName().startsWith(OdooNames.IR_MODEL_ACCESS) && !getText().isEmpty()) {
            CsvFile csvFile = (CsvFile) file;
            CsvColumnInfoMap<PsiElement> columnInfoMap = csvFile.getColumnInfoMap();
            CsvColumnInfo<PsiElement> columnInfo = columnInfoMap.getColumnInfo(this);
            PsiElement header = columnInfo.getHeaderElement();
            if (header != null && header != this) {
                String headerText = header.getText();
                if ("model_id:id".equals(headerText)) {
                    return new OdooExternalIdReference(this, null, OdooRecordFilters.IR_MODEL, true);
                } else if ("group_id:id".equals(headerText)) {
                    return new OdooExternalIdReference(this, null, OdooRecordFilters.RES_GROUPS, true);
                }
            }
        }
        return super.getReference();
    }
}
