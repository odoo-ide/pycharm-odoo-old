package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.*;
import com.jetbrains.python.psi.PyClass;
import dev.ngocta.pycharm.odoo.data.OdooRecordElement;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClass;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;

import java.util.Optional;

@Referencing(OdooFieldValueReferenceConverter.class)
public interface OdooDomFieldAssignment extends OdooDomField, GenericDomValue<String> {
    @Attribute("ref")
    @Referencing(OdooFieldRefValueReferenceConverter.class)
    GenericAttributeValue<String> getRefAttr();

    @Attribute("file")
    @Referencing(OdooFieldFileValueReferenceConverter.class)
    GenericAttributeValue<String> getFileAttr();

    default String getModel() {
        OdooDomRecord record = getRecord();
        if (record != null) {
            return record.getModel();
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
        return Optional.of(getRefAttr())
                .map(GenericAttributeValue::getXmlAttributeValue)
                .map(PsiElement::getReference)
                .map(PsiReference::resolve)
                .filter(OdooRecordElement.class::isInstance)
                .map(PsiElement::getNavigationElement)
                .filter(PyClass.class::isInstance)
                .map(OdooModelUtils::getContainingOdooModelClass)
                .map(OdooModelClass::getName)
                .orElse(null);
    }
}
