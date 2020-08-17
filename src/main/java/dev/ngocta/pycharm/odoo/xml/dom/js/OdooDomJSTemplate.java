package dev.ngocta.pycharm.odoo.xml.dom.js;

import com.intellij.psi.PsiTarget;
import com.intellij.util.ObjectUtils;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomTarget;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.NameValue;
import dev.ngocta.pycharm.odoo.xml.OdooJSTemplateElement;
import org.jetbrains.annotations.Nullable;

public interface OdooDomJSTemplate extends OdooDomJSTemplateElement {
    @NameValue
    @Attribute("t-name")
    GenericAttributeValue<String> getNameAttribute();

    @Attribute("t-extend")
    GenericAttributeValue<String> getExtendAttribute();

    @Attribute("t-inherit")
    GenericAttributeValue<String> getInheritAttribute();

    @Nullable
    default String getName() {
        if (getNameAttribute().getStringValue() != null) {
            return getNameAttribute().getStringValue();
        }
        if (getInheritAttribute().getStringValue() != null) {
            String[] splits = getInheritAttribute().getStringValue().split("\\.", 2);
            return splits.length > 1 ? splits[1] : splits[0];
        }
        return getExtendAttribute().getStringValue();
    }

    @Nullable
    default String getInherit() {
        return ObjectUtils.chooseNotNull(getInheritAttribute().getStringValue(), getExtendAttribute().getStringValue());
    }

    default boolean isOldInheritance() {
        return getExtendAttribute().getStringValue() != null;
    }

    @Nullable
    default OdooJSTemplateElement getNavigationElement() {
        PsiTarget psiTarget = DomTarget.getTarget(this);
        return psiTarget == null ? null : new OdooJSTemplateElement(psiTarget);
    }
}
