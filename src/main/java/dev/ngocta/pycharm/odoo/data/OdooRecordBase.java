package dev.ngocta.pycharm.odoo.data;

import org.jetbrains.annotations.NotNull;

public class OdooRecordBase implements OdooRecord {
    private final String myId;
    private final String myModel;

    public OdooRecordBase(@NotNull String id, @NotNull String model) {
        myId = id;
        myModel = model;
    }

    @Override
    public String getId() {
        return myId;
    }

    @Override
    public String getModel() {
        return myModel;
    }
}
