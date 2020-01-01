package dev.ngocta.pycharm.odoo.data;

import com.google.common.collect.ImmutableMap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.xml.*;
import com.jetbrains.python.psi.PyTargetExpression;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.model.OdooFieldInfo;
import dev.ngocta.pycharm.odoo.model.OdooModelReference;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OdooFieldValueReferenceConverter implements CustomReferenceConverter<String> {
    private static final ImmutableMap<String, String> modelNameFields = ImmutableMap.<String, String>builder()
            .put(OdooNames.IR_UI_VIEW_MODEL, OdooNames.IR_UI_VIEW)
            .put(OdooNames.IR_ACTIONS_ACT_WINDOW_RES_MODEL, OdooNames.IR_ACTIONS_ACT_WINDOW)
            .build();

    @NotNull
    @Override
    public PsiReference[] createReferences(GenericDomValue<String> value, PsiElement element, ConvertContext context) {
        if (value instanceof OdooDomField) {
            String fieldName = ((OdooDomField) value).getName().getStringValue();
            String model = modelNameFields.get(fieldName);
            if (model != null) {
                DomElement parent = value.getParent();
                if (parent instanceof OdooRecord) {
                    String m = ((OdooRecord) parent).getModel();
                    if (model.equals(m)) {
                        return new PsiReference[]{new OdooModelReference(element)};
                    }
                }
            }
        } else if (value instanceof GenericAttributeValue<?>) {
            OdooDomField field = value.getParentOfType(OdooDomField.class, true);
            if (field != null) {
                XmlAttribute attribute = ((GenericAttributeValue<?>) value).getXmlAttribute();
                if (attribute != null && OdooNames.XML_FIELD_ATTR_REF.equals(attribute.getName())) {
                    String model = null;
                    PsiElement targetField = Optional.ofNullable(field.getName())
                            .map(GenericAttributeValue::getXmlAttributeValue)
                            .map(PsiElement::getReference)
                            .map(PsiReference::resolve)
                            .orElse(null);
                    if (targetField instanceof PyTargetExpression) {
                        OdooFieldInfo fieldInfo = OdooFieldInfo.getInfo((PyTargetExpression) targetField);
                        if (fieldInfo != null && OdooNames.FIELD_TYPE_MANY2ONE.equals(fieldInfo.getTypeName())) {
                            model = fieldInfo.getComodel();
                        }
                    }
                    return new PsiReference[]{new OdooExternalIdReference(element, model, null, true)};
                }
            }
        }
        return new PsiReference[0];
    }
}
