package dev.ngocta.pycharm.odoo.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class OdooRecordBase implements OdooRecord {
    private final String myId;
    private final String myModel;
    private final OdooRecordSubType mySubType;

    public OdooRecordBase(@NotNull String id, @NotNull String model, @Nullable OdooRecordSubType subType) {
        myId = id;
        myModel = model;
        mySubType = subType;
    }

    @Override
    public String getId() {
        return myId;
    }

    @Override
    public String getModel() {
        return myModel;
    }

    public OdooRecordSubType getSubType() {
        return mySubType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooRecordBase that = (OdooRecordBase) o;
        return myId.equals(that.myId) &&
                Objects.equals(myModel, that.myModel) &&
                mySubType == that.mySubType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(myId, myModel, mySubType);
    }
}
