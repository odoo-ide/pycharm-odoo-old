package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.PlatformIcons;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Optional;

public class OdooRecordPresentation implements ItemPresentation {
    private final OdooRecord myRecord;
    private final Project myProject;

    public OdooRecordPresentation(@NotNull OdooRecord record,
                                  @NotNull Project project) {
        myRecord = record;
        myProject = project;
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
                    OdooModule module = OdooModule.findModule(file, myProject);
                    if (module != null) {
                        path = "/" + module.getName() + path.substring(module.getDirectory().getVirtualFile().getPath().length());
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
