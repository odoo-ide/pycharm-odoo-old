package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.DomFileDescription;

public class OdooDomFileMetaData extends DomFileDescription<OdooDomFile> {
    public OdooDomFileMetaData() {
        super(OdooDomFile.class, OdooDomFile.NAME);
    }
}
