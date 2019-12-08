package dev.ngocta.pycharm.odoo.data;

public interface OdooDomMenuItem extends OdooDomRecordShortcut {
    @Override
    default String getModel() {
        return "ir.ui.menu";
    }
}
