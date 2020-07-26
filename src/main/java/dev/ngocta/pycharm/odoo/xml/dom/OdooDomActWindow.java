package dev.ngocta.pycharm.odoo.xml.dom;

import dev.ngocta.pycharm.odoo.OdooNames;

public interface OdooDomActWindow extends OdooDomRecordLike {
    @Override
    default String getModel() {
        return OdooNames.IR_ACTIONS_ACT_WINDOW;
    }
}
