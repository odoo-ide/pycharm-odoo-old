package dev.ngocta.pycharm.odoo.xml;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;

public interface OdooDomRecord extends DomElement {
    @Attribute("id")
    GenericAttributeValue<String> getId();

    @Attribute("model")
    GenericAttributeValue<String> getModel();

    default String getQualifiedId(@NotNull VirtualFile xmlFile) {
        String id = getId().getValue();
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
