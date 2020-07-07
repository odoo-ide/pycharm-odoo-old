package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.DomFileDescription;

public class OdooDomFileMetaData extends DomFileDescription<OdooDomRoot> {
    public OdooDomFileMetaData() {
        super(OdooDomRoot.class, OdooDomRoot.NAME);
    }
}
