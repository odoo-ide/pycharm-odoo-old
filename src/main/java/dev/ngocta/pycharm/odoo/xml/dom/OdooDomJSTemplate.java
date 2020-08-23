package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiTarget;
import com.intellij.util.ObjectUtils;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.DomTarget;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.NameValue;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.OdooJSTemplateElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OdooDomJSTemplate extends OdooDomElement {
    @NameValue
    @Attribute("t-name")
    GenericAttributeValue<String> getNameAttribute();

    @Attribute("t-extend")
    GenericAttributeValue<String> getExtendAttribute();

    @Attribute("t-inherit")
    GenericAttributeValue<String> getInheritAttribute();

    @Nullable
    default String getName() {
        return getNameAttribute().getStringValue();
    }

    @Nullable
    default String getQualifiedName() {
        String name = getName();
        if (name == null) {
            return null;
        }
        OdooModule module = OdooModuleUtils.getContainingOdooModule(getXmlElement());
        if (module == null) {
            return name;
        }
        String[] splits = name.split("\\.", 2);
        if (splits.length > 1 && module.getName().equals(splits[0])) {
            return name;
        }
        return module.getName() + "." + name;
    }

    default boolean isTemplateOf(@NotNull String name,
                                 boolean isQualified) {
        return (isQualified && name.equals(getQualifiedName())) || (!isQualified && name.equals(getName()));
    }

    @Nullable
    default String getInheritName() {
        return ObjectUtils.chooseNotNull(getInheritAttribute().getStringValue(), getExtendAttribute().getStringValue());
    }

    default boolean isNewInheritanceMechanism() {
        return getInheritAttribute().exists();
    }

    @Nullable
    default OdooJSTemplateElement getNavigationElement() {
        PsiTarget psiTarget = null;
        if (getNameAttribute().getStringValue() != null) {
            psiTarget = DomTarget.getTarget(this);
        } else if (getInheritAttribute().getStringValue() != null) {
            psiTarget = DomTarget.getTarget(this, getInheritAttribute());
        } else if (getExtendAttribute().getStringValue() != null) {
            psiTarget = DomTarget.getTarget(this, getExtendAttribute());
        }
        return psiTarget == null ? null : new OdooJSTemplateElement(psiTarget);
    }
}
