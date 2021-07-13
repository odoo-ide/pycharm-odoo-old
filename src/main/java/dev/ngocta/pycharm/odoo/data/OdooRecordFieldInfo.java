package dev.ngocta.pycharm.odoo.data;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OdooRecordFieldInfo extends OdooRecordExtraInfo {
    private final String myFieldName;
    private final String myModelName;

    public OdooRecordFieldInfo(@NotNull String fieldName,
                               @NotNull String modelName) {
        myFieldName = fieldName;
        myModelName = modelName;
    }

    @NotNull
    public String getFieldName() {
        return myFieldName;
    }

    @NotNull
    public String getModelName() {
        return myModelName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooRecordFieldInfo that = (OdooRecordFieldInfo) o;
        return Objects.equals(myFieldName, that.myFieldName)
                && Objects.equals(myModelName, that.myModelName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myFieldName, myModelName);
    }
}
