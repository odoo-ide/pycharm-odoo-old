package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.SubTagList;

import java.util.List;

public interface OdooDomRecordContainer {
    @SubTagList("record")
    List<OdooDomRecord> getRecords();
}
