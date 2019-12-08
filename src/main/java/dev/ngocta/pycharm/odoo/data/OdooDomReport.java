package dev.ngocta.pycharm.odoo.data;

public interface OdooDomReport extends OdooDomRecordShortcut {
    @Override
    default String getModel() {
        return "ir.actions.report";
    }
}
