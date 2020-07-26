package dev.ngocta.pycharm.odoo.data.filter;

import com.intellij.util.ObjectUtils;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.data.OdooRecordViewInfo;

public class OdooRecordFilters {
    public static final OdooRecordFilter RES_GROUPS = new OdooRecordModelFilter(OdooNames.RES_GROUPS);
    public static final OdooRecordFilter IR_UI_MENU = new OdooRecordModelFilter(OdooNames.IR_UI_VIEW);
    public static final OdooRecordFilter ACTION_MODELS = new OdooRecordModelFilter(OdooNames.ACTION_MODELS);
    public static final OdooRecordFilter QWEB = record -> {
        OdooRecordViewInfo viewInfo = ObjectUtils.tryCast(record.getExtraInfo(), OdooRecordViewInfo.class);
        return viewInfo != null && OdooNames.VIEW_TYPE_QWEB.equals(viewInfo.getViewType());
    };
}
