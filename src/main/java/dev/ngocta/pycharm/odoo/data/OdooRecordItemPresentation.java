package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
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
        String text = myDefinition.getName();
        if (StringUtil.isNotEmpty(myDefinition.getModel())) {
            text += " (" + myDefinition.getModel() + ")";
        }
        return text;
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
