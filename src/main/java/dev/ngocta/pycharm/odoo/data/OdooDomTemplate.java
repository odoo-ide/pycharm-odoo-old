package dev.ngocta.pycharm.odoo.data;

public interface OdooDomTemplate extends OdooDomRecordShortcut {
    @Override
    default String getModel() {
        return "ir.ui.view";
    }
}
