package dev.ngocta.pycharm.odoo.data;

import dev.ngocta.pycharm.odoo.OdooNames;

public interface OdooDomTemplate extends OdooDomRecordShortcut {
    @Override
    default String getModel() {
        return OdooNames.MODEL_IR_UI_VIEW;
    }
}
