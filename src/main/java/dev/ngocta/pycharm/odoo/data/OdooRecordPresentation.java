package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PlatformIcons;
import dev.ngocta.pycharm.odoo.OdooUtils;

import javax.swing.*;
import java.util.Optional;

public class OdooRecordPresentation implements ItemPresentation {
    private final OdooRecord myRecord;

    public OdooRecordPresentation(OdooRecord record) {
        myRecord = record;
    }

    @Override
    public String getPresentableText() {
        String text = myRecord.getId();
        if (StringUtil.isNotEmpty(myRecord.getModel())) {
            text += " (" + myRecord.getModel() + ")";
        }
        return text;
    }

    @Override
    public String getLocationString() {
        return Optional.ofNullable(myRecord.getDataFile())
                .map(file -> {
                    String path = file.getPath();
                    VirtualFile moduleDir = OdooUtils.getOdooModuleDirectory(file);
                    if (moduleDir != null) {
                        path = "/" + moduleDir.getName() + path.substring(moduleDir.getPath().length());
                    }
                    return path;
                })
                .orElse(null);
    }

    @Override
    public Icon getIcon(boolean unused) {
        return PlatformIcons.XML_TAG_ICON;
    }
}
