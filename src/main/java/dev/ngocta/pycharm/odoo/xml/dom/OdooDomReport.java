package dev.ngocta.pycharm.odoo.xml.dom;

import dev.ngocta.pycharm.odoo.OdooNames;

public interface OdooDomReport extends OdooDomRecordLike {
    @Override
    default String getModel() {
        return OdooNames.IR_ACTIONS_REPORT;
    }
}
