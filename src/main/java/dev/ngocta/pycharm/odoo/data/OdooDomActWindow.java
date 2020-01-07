package dev.ngocta.pycharm.odoo.data;

import dev.ngocta.pycharm.odoo.OdooNames;

public interface OdooDomActWindow extends OdooDomRecordLike {
    @Override
    default OdooRecord getRecord() {
        return getRecord(OdooNames.IR_ACTIONS_ACT_WINDOW, null);
    }
}
