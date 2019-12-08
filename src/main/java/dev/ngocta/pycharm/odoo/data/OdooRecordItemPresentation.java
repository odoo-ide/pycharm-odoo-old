package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformIcons;

import javax.swing.*;

public class OdooRecordItemPresentation implements ItemPresentation {
    private final OdooRecordItem myDefinition;

    public OdooRecordItemPresentation(OdooRecordItem definition) {
        myDefinition = definition;
    }

    @Override
    public String getPresentableText() {
        return myDefinition.getName() + " (" + myDefinition.getModel() + ")";
    }

    @Override
    public String getLocationString() {
        VirtualFile file = myDefinition.getFile();
        if (file != null) {
            return file.getName();
        }
        return null;
    }

    @Override
    public Icon getIcon(boolean unused) {
        return PlatformIcons.XML_TAG_ICON;
    }
}
