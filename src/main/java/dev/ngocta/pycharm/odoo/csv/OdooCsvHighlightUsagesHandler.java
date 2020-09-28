package dev.ngocta.pycharm.odoo.csv;

import com.intellij.openapi.editor.Editor;
import net.seesharpsoft.intellij.plugins.csv.highlighter.CsvHighlightUsagesHandler;
import net.seesharpsoft.intellij.plugins.csv.psi.CsvFile;
import org.jetbrains.annotations.NotNull;

public class OdooCsvHighlightUsagesHandler extends CsvHighlightUsagesHandler {
    protected OdooCsvHighlightUsagesHandler(@NotNull Editor editor, @NotNull CsvFile file) {
        super(editor, file);
    }

    @Override
    public boolean highlightReferences() {
        return true;
    }
}
