package dev.ngocta.pycharm.odoo.data;

import dev.ngocta.pycharm.odoo.OdooNames;

public interface OdooDomTemplate extends OdooDomRecordShortcut {
    @Override
    default String getModel() {
        return OdooNames.IR_UI_VIEW;
    }

    @Override
    default OdooRecordSubType getSubType() {
        return OdooRecordSubType.VIEW_QWEB;
    }
}
