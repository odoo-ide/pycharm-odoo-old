package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;

import java.util.LinkedList;
import java.util.List;

public interface OdooDomRoot extends DomElement, OdooDomRecordContainer {
    String NAME = "odoo";

    @SubTagList("data")
    List<OdooDomData> getData();

    default List<OdooDomRecord> getAllRecords() {
        List<OdooDomRecord> result = new LinkedList<>(getRecords());
        getData().forEach(data -> result.addAll(data.getRecords()));
        return result;
    }
}
