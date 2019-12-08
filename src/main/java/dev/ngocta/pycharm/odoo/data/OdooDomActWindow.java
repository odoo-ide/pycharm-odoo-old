package dev.ngocta.pycharm.odoo.data;

public interface OdooDomActWindow extends OdooDomRecordShortcut {
    @Override
    default String getModel() {
        return "ir.actions.act_window";
    }
}
