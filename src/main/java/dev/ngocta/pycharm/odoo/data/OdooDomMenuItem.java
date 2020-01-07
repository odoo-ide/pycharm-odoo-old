package dev.ngocta.pycharm.odoo.data;

import dev.ngocta.pycharm.odoo.OdooNames;

public interface OdooDomMenuItem extends OdooDomRecordLike {
    @Override
    default OdooRecord getRecord() {
        return getRecord(OdooNames.IR_UI_MENU, null);
    }
}
