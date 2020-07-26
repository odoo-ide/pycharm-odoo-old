package dev.ngocta.pycharm.odoo.data.filter;

import com.intellij.util.ObjectUtils;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.data.OdooRecordViewInfo;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class OdooRecordViewModelFilter implements OdooRecordFilter {
    private final String myViewModel;
    private final List<String> myExcludedIds;

    public OdooRecordViewModelFilter(@NotNull String viewModel,
                                     String... excludedIds) {
        myViewModel = viewModel;
        myExcludedIds = Arrays.asList(excludedIds);
    }

    @Override
    public boolean accept(@NotNull OdooRecord record) {
        OdooRecordViewInfo viewInfo = ObjectUtils.tryCast(record.getExtraInfo(), OdooRecordViewInfo.class);
        return viewInfo != null && myViewModel.equals(viewInfo.getViewModel()) && !myExcludedIds.contains(record.getQualifiedId());
    }
}
