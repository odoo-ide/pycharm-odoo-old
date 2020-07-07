package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.data.OdooRecordImpl;
import dev.ngocta.pycharm.odoo.data.OdooRecordSubType;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.Nullable;

public interface OdooDomRecordLike extends OdooDomElement {
    @Attribute("id")
    @Required
    GenericAttributeValue<String> getId();

    @Nullable
    default OdooRecord getRecord(@Nullable String model,
                                 @Nullable OdooRecordSubType subType) {
        String id = getId().getValue();
        XmlElement element = getXmlElement();
        if (id == null || element == null) {
            return null;
        }
        OdooModule module = OdooModuleUtils.getContainingOdooModule(element);
        if (module != null) {
            VirtualFile file = element.getContainingFile().getVirtualFile();
            return new OdooRecordImpl(id, StringUtil.notNullize(model), subType, module.getName(), file);
        }
        return null;
    }

    @Nullable
    default OdooRecord getRecord() {
        return getRecord(null, null);
    }
}
