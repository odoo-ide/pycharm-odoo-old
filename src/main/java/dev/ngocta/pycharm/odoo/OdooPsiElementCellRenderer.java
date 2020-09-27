package dev.ngocta.pycharm.odoo;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.popup.IPopupChooserBuilder;

public class OdooPsiElementCellRenderer extends DefaultPsiElementCellRenderer {
    @Override
    public void installSpeedSearch(IPopupChooserBuilder builder) {
        installSpeedSearch(builder, true);
    }
}
