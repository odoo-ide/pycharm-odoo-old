package dev.ngocta.pycharm.odoo.data;

import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutput;
import java.io.IOException;
import java.util.Objects;

public class OdooRecordViewInfo extends OdooRecordExtraInfo {
    private final String myViewType;
    private final String myViewModel;
    private final String myInheritId;

    public OdooRecordViewInfo(@Nullable String viewType,
                              @Nullable String viewModel,
                              @Nullable String inheritId) {
        myViewType = viewType;
        myViewModel = viewModel;
        myInheritId = inheritId;
    }

    @Nullable
    public String getViewType() {
        return myViewType;
    }

    @Nullable
    public String getViewModel() {
        return myViewModel;
    }

    @Nullable
    public String getInheritId() {
        return myInheritId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooRecordViewInfo that = (OdooRecordViewInfo) o;
        return Objects.equals(myViewType, that.myViewType) &&
                Objects.equals(myViewModel, that.myViewModel) &&
                Objects.equals(myInheritId, that.myInheritId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myViewType, myViewModel, myInheritId);
    }

    @Override
    void write(@NotNull DataOutput out) throws IOException {
        OdooUtils.writeNullableString(getViewType(), out);
        OdooUtils.writeNullableString(getViewModel(), out);
        OdooUtils.writeNullableString(getInheritId(), out);
    }
}
