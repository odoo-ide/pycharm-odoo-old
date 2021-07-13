package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.io.DataExternalizer;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class OdooRecordModelInfoExternalizer implements DataExternalizer<OdooRecordModelInfo> {
    public static final OdooRecordModelInfoExternalizer INSTANCE = new OdooRecordModelInfoExternalizer();

    @Override
    public void save(@NotNull DataOutput out,
                     OdooRecordModelInfo value) throws IOException {
        OdooUtils.writeNullableString(value.getModelName(), out);
    }

    @Override
    public OdooRecordModelInfo read(@NotNull DataInput in) throws IOException {
        String modelName = OdooUtils.readNullableString(in);
        if (modelName != null) {
            return new OdooRecordModelInfo(modelName);
        }
        return null;
    }
}
