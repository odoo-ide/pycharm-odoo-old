package dev.ngocta.pycharm.odoo.csv;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import net.seesharpsoft.intellij.plugins.csv.highlighter.CsvHighlightUsagesHandlerFactory;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooCsvHighlightUsagesHandlerFactory extends CsvHighlightUsagesHandlerFactory {
    @Override
    @Nullable
    public HighlightUsagesHandlerBase createHighlightUsagesHandler(@NotNull Editor editor,
                                                                   @NotNull PsiFile psiFile) {
        if (psiFile instanceof CsvFile && OdooModuleUtils.isInOdooModule(psiFile)) {
            return new OdooCsvHighlightUsagesHandler(editor, (CsvFile) psiFile);
        }
        return super.createHighlightUsagesHandler(editor, psiFile);
    }
}
