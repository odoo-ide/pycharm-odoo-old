package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.io.DataExternalizer;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class OdooRecordViewInfoExternalizer implements DataExternalizer<OdooRecordViewInfo> {
    public static final OdooRecordViewInfoExternalizer INSTANCE = new OdooRecordViewInfoExternalizer();

    @Override
    public void save(@NotNull DataOutput out,
                     OdooRecordViewInfo value) throws IOException {
        OdooUtils.writeNullableString(value.getViewType(), out);
        OdooUtils.writeNullableString(value.getViewModel(), out);
        OdooUtils.writeNullableString(value.getInheritId(), out);
    }

    @Override
    public OdooRecordViewInfo read(@NotNull DataInput in) throws IOException {
        String viewType = OdooUtils.readNullableString(in);
        String viewModel = OdooUtils.readNullableString(in);
        String inheritId = OdooUtils.readNullableString(in);
        return new OdooRecordViewInfo(viewType, viewModel, inheritId);
    }
}
