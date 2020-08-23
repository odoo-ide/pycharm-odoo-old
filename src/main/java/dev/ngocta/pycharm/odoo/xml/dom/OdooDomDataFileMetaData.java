package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.DomFileDescription;

public class OdooDomDataFileMetaData extends DomFileDescription<OdooDomDataFile> {
    public OdooDomDataFileMetaData() {
        super(OdooDomDataFile.class, OdooDomDataFile.NAME);
    }
}
