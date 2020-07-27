package dev.ngocta.pycharm.odoo.data;

import org.jetbrains.annotations.Nullable;

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
}
