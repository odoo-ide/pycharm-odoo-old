package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;
import com.intellij.util.xml.SubTagsList;

import java.util.List;

public interface OdooDomOperationContainer extends DomElement {
    @SubTagList("record")
    List<OdooDomRecord> getRecords();

    @SubTagList("template")
    List<OdooDomTemplate> getTemplates();

    @SubTagList("menuitem")
    List<OdooDomMenuItem> getMenuItems();

    @SubTagList("report")
    List<OdooDomReport> getReports();

    @SubTagList("act_window")
    List<OdooDomActWindow> getActWindows();

    @SubTagsList({"record", "template", "menuitem", "report", "act_window"})
    List<OdooDomRecordLike> getRecordLikeItems();
}
