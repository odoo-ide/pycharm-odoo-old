package dev.ngocta.pycharm.odoo.data;

import com.intellij.util.xml.DomFileDescription;

public class OdooDomFileMetaData extends DomFileDescription<OdooDomRoot> {
    public OdooDomFileMetaData() {
        super(OdooDomRoot.class, OdooDomRoot.NAME);
    }
}
