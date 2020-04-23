package dev.ngocta.pycharm.odoo.data;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.Attribute;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.Referencing;
import com.intellij.util.xml.Required;
import com.jetbrains.python.psi.PyTargetExpression;
import dev.ngocta.pycharm.odoo.model.OdooFieldInfo;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public interface OdooDomField extends OdooDomElement, OdooDomModelScoped {
    @Attribute("name")
    @Required
    @Referencing(OdooFieldNameReferenceConverter.class)
    GenericAttributeValue<String> getName();

    @Nullable
    default String getComodel() {
        PsiElement targetField = Optional.ofNullable(getName())
                .map(GenericAttributeValue::getXmlAttributeValue)
                .map(PsiElement::getReference)
                .map(PsiReference::resolve)
                .orElse(null);
        if (targetField instanceof PyTargetExpression) {
            OdooFieldInfo fieldInfo = OdooFieldInfo.getInfo((PyTargetExpression) targetField);
            if (fieldInfo != null) {
                return fieldInfo.getComodel();
            }
        }
        return null;
    }
}
