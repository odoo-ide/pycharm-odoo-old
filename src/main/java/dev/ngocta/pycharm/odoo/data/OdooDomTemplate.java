package dev.ngocta.pycharm.odoo.data;

import dev.ngocta.pycharm.odoo.OdooNames;

public interface OdooDomTemplate extends OdooDomRecordLike {
    @Override
    default OdooRecord getRecord() {
        return getRecord(OdooNames.IR_UI_VIEW, OdooRecordSubType.VIEW_QWEB);
    }
}
