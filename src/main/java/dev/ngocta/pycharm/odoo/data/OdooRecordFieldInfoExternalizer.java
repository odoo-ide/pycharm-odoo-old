package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.io.DataExternalizer;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

class OdooRecordFieldInfoExternalizer implements DataExternalizer<OdooRecordFieldInfo> {
    public static final OdooRecordFieldInfoExternalizer INSTANCE = new OdooRecordFieldInfoExternalizer();

    @Override
    public void save(@NotNull DataOutput out,
                     OdooRecordFieldInfo value) throws IOException {
        OdooUtils.writeNullableString(value.getFieldName(), out);
        OdooUtils.writeNullableString(value.getModelName(), out);
    }

    @Override
    public OdooRecordFieldInfo read(@NotNull DataInput in) throws IOException {
        String fieldName = OdooUtils.readNullableString(in);
        String modelName = OdooUtils.readNullableString(in);
        if (fieldName != null && modelName != null) {
            return new OdooRecordFieldInfo(fieldName, modelName);
        }
        return null;
    }
}
