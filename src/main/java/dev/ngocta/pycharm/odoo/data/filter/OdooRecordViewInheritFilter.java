package dev.ngocta.pycharm.odoo.data.filter;

import com.intellij.util.ObjectUtils;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.data.OdooRecordViewInfo;
import org.jetbrains.annotations.NotNull;

public class OdooRecordViewInheritFilter implements OdooRecordFilter {
    private final String myInheritId;

    public OdooRecordViewInheritFilter(@NotNull String inheritId) {
        myInheritId = inheritId;
    }

    @Override
    public boolean accept(@NotNull OdooRecord record) {
        OdooRecordViewInfo viewInfo = ObjectUtils.tryCast(record.getExtraInfo(), OdooRecordViewInfo.class);
        return viewInfo != null && myInheritId.equals(viewInfo.getInheritId());
    }
}
