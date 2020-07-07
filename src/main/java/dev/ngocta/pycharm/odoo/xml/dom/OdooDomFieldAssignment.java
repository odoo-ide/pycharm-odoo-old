package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.*;
import com.jetbrains.python.psi.PyClass;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClass;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;

import java.util.Optional;

@Referencing(OdooFieldValueReferenceConverter.class)
public interface OdooDomFieldAssignment extends OdooDomField, GenericDomValue<String> {
    @Attribute("ref")
    @Referencing(OdooFieldRefValueReferenceConverter.class)
    GenericAttributeValue<String> getRef();

    @Attribute("file")
    @Referencing(OdooFieldFileValueReferenceConverter.class)
    GenericAttributeValue<String> getFile();

    default String getModel() {
        OdooDomRecord record = getRecord();
        if (record != null) {
            return record.getModel().getStringValue();
        }
        return null;
    }

    default OdooDomRecord getRecord() {
        DomElement parent = getParent();
        if (parent instanceof OdooDomRecord) {
            return (OdooDomRecord) parent;
        }
        return null;
    }

    default String getRefModel() {
        return Optional.of(getRef())
                .map(GenericAttributeValue::getXmlAttributeValue)
                .map(PsiElement::getReference)
                .map(PsiReference::resolve)
                .filter(PyClass.class::isInstance)
                .map(OdooModelUtils::getContainingOdooModelClass)
                .map(OdooModelClass::getName)
                .orElse(null);
    }
}
