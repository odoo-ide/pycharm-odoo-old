package dev.ngocta.pycharm.odoo.data;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OdooRecordModelInfo extends OdooRecordExtraInfo {
    private final String myModelName;

    public OdooRecordModelInfo(@NotNull String modelName) {
        myModelName = modelName;
    }

    @NotNull
    public String getModelName() {
        return myModelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooRecordModelInfo that = (OdooRecordModelInfo) o;
        return Objects.equals(myModelName, that.myModelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myModelName);
    }
}
