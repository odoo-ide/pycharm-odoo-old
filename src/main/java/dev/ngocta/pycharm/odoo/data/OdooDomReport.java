package dev.ngocta.pycharm.odoo.data;

import dev.ngocta.pycharm.odoo.OdooNames;

public interface OdooDomReport extends OdooDomRecordLike {
    @Override
    default OdooRecord getRecord() {
        return getRecord(OdooNames.IR_ACTIONS_REPORT, null);
    }
}
