package dev.ngocta.pycharm.odoo.data.filter;

import dev.ngocta.pycharm.odoo.data.OdooRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class OdooRecordModelFilter implements OdooRecordFilter {
    private final List<String> myModels;

    public OdooRecordModelFilter(@Nullable String... models) {
        myModels = new LinkedList<>();
        for (String model : models) {
            if (model != null) {
                myModels.add(model);
            }
        }
    }

    @Override
    public boolean accept(@NotNull OdooRecord record) {
        return myModels.isEmpty() || myModels.contains(record.getModel());
    }
}
