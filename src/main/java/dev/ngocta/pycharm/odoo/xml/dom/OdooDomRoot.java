package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.SubTagList;

import java.util.LinkedList;
import java.util.List;

public interface OdooDomRoot extends OdooDomOperationContainer {
    String NAME = "odoo";

    @SubTagList("data")
    List<OdooDomData> getGroups();

    default List<OdooDomRecordLike> getAllRecordLikeItems() {
        List<OdooDomRecordLike> result = new LinkedList<>(DomUtil.getChildrenOf(this, OdooDomRecordLike.class));
        getGroups().forEach(group -> result.addAll(DomUtil.getChildrenOfType(group, OdooDomRecordLike.class)));
        return result;
    }
}
