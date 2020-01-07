package dev.ngocta.pycharm.odoo.data;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Required;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface OdooDomRecordLike extends DomElement {
    @Attribute("id")
    @Required
    GenericAttributeValue<String> getId();

    default OdooRecord getRecord(@Nullable String model, @Nullable OdooRecordSubType subType) {
        String id = getId().getValue();
        XmlElement element = getXmlElement();
        if (id == null || element == null) {
            return null;
        }
        String module = Optional.ofNullable(OdooUtils.getOdooModule(element))
                .map(PsiDirectory::getName)
                .orElse(null);
        if (module != null) {
            return new OdooRecordImpl(id, model, subType, module, null);
        }
        return null;
    }

    default OdooRecord getRecord() {
        return getRecord(null, null);
    }
}
