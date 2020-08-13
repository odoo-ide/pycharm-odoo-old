package dev.ngocta.pycharm.odoo.csv;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooCsvFieldManipulator extends AbstractElementManipulator<OdooCsvField> {
    @Nullable
    @Override
    public OdooCsvField handleContentChange(@NotNull OdooCsvField element,
                                            @NotNull TextRange range,
                                            String newContent) throws IncorrectOperationException {
        return null;
    }
}
