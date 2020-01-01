package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;

public interface OdooDomRecord extends DomElement, OdooRecord {
    @Attribute("id")
    @Required
    GenericAttributeValue<String> getIdAttr();

    @Override
    default String getId() {
        return getIdAttr().getValue();
    }

    default String getQualifiedId(@NotNull VirtualFile xmlFile) {
        String id = getId();
        if (id != null && !id.contains(".")) {
            VirtualFile moduleDir = OdooUtils.getOdooModuleDirectory(xmlFile);
            if (moduleDir != null) {
                id = moduleDir.getName() + "." + id;
            }
        }
        return id;
    }

    default String getQualifiedId() {
        XmlTag tag = getXmlTag();
        if (tag != null) {
            PsiFile file = tag.getContainingFile();
            if (file != null) {
                return getQualifiedId(file.getVirtualFile());
            }
        }
        return null;
    }
}
