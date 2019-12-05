package dev.ngocta.pycharm.odoo.xml;

import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.SubTagList;

import java.util.List;

public interface OdooRootTag extends DomElement, OdooRecordTagContainer {
    String NAME = "odoo";

    @SubTagList("data")
    List<OdooDataTag> getData();
}
