package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.openapi.module.Module;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooDomJSTemplateFileMetaData extends DomFileDescription<OdooDomJSTemplateFile> {
    public OdooDomJSTemplateFileMetaData() {
        super(OdooDomJSTemplateFile.class, "");
    }

    @Override
    public boolean acceptsOtherRootTagNames() {
        return true;
    }

    @Override
    public boolean isMyFile(@NotNull XmlFile file,
                            @Nullable Module module) {
        XmlTag xmlTag = file.getRootTag();
        return xmlTag != null && ArrayUtil.contains(xmlTag.getName(), "template", "templates");
    }
}
