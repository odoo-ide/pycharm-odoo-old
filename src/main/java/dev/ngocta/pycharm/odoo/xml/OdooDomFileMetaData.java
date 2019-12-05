package dev.ngocta.pycharm.odoo.xml;

import com.intellij.util.xml.DomFileDescription;

public class OdooDomFileMetaData extends DomFileDescription<OdooRootTag> {
    public OdooDomFileMetaData() {
        super(OdooRootTag.class, OdooRootTag.NAME);
    }
}
