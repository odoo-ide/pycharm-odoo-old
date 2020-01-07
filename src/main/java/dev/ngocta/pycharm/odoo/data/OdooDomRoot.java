package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.SubTagList;

import java.util.LinkedList;
import java.util.List;

public interface OdooDomRoot extends OdooDomOperationContainer {
    String NAME = "odoo";

    @SubTagList("data")
    List<OdooDomData> getGroups();

    default List<OdooDomRecordLike> getAllRecordLikeItems() {
        List<OdooDomRecordLike> result = new LinkedList<>(getRecordLikeItems());
        getGroups().forEach(group -> result.addAll(group.getRecordLikeItems()));
        return result;
    }
}
