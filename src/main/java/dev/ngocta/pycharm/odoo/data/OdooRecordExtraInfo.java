package dev.ngocta.pycharm.odoo.data;

import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public abstract class OdooRecordExtraInfo {
    abstract void write(@NotNull DataOutput out) throws IOException;

    public static OdooRecordExtraInfo read(@NotNull String model,
                                           @NotNull DataInput in) throws IOException {
        if (OdooNames.IR_UI_VIEW.equals(model)) {
            String viewType = OdooUtils.readNullableString(in);
            String viewModel = OdooUtils.readNullableString(in);
            String inheritId = OdooUtils.readNullableString(in);
            return new OdooRecordViewInfo(viewType, viewModel, inheritId);
        }
        return null;
    }
}
