package dev.ngocta.pycharm.odoo.data;

import org.jetbrains.annotations.Nullable;

public interface OdooRecord {
    String getId();

    String getModel();

    @Nullable
    default OdooRecordSubType getSubType() {
        return null;
    }
}
