package dev.ngocta.pycharm.odoo.data.filter;

import dev.ngocta.pycharm.odoo.data.OdooRecord;
import org.jetbrains.annotations.NotNull;

public interface OdooRecordFilter {
    boolean accept(@NotNull OdooRecord record);
}

