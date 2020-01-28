package dev.ngocta.pycharm.odoo.data;

import com.google.common.collect.ImmutableMap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericAttributeValue;
import com.intellij.util.xml.GenericDomValue;
import com.jetbrains.python.psi.PyTargetExpression;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.model.OdooFieldInfo;
import dev.ngocta.pycharm.odoo.model.OdooModelReference;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class OdooFieldValueReferenceConverter implements CustomReferenceConverter<String> {
    private static final ImmutableMap<String, String> knownModelNameFields = ImmutableMap.<String, String>builder()
            .put("model", OdooNames.IR_UI_VIEW)
            .put("res_model", OdooNames.IR_ACTIONS_ACT_WINDOW)
            .build();

    @NotNull
    @Override
    public PsiReference[] createReferences(GenericDomValue<String> value, PsiElement element, ConvertContext context) {
        if (value instanceof OdooDomFieldAssignment) {
            String fieldName = ((OdooDomFieldAssignment) value).getName().getStringValue();
            String model = knownModelNameFields.get(fieldName);
            if (model != null && model.equals(((OdooDomFieldAssignment) value).getModel())) {
                return new PsiReference[]{new OdooModelReference(element)};
            }
        } else if (value instanceof GenericAttributeValue<?>) {
            OdooDomFieldAssignment field = value.getParentOfType(OdooDomFieldAssignment.class, true);
            if (field != null) {
                if ("ref".equals(value.getXmlElementName())) {
                    PsiElement targetField = Optional.ofNullable(field.getName())
                            .map(GenericAttributeValue::getXmlAttributeValue)
                            .map(PsiElement::getReference)
                            .map(PsiReference::resolve)
                            .orElse(null);
                    if (targetField instanceof PyTargetExpression) {
                        OdooFieldInfo fieldInfo = OdooFieldInfo.getInfo((PyTargetExpression) targetField);
                        if (fieldInfo != null && OdooNames.FIELD_TYPE_MANY2ONE.equals(fieldInfo.getTypeName())) {
                            String model = fieldInfo.getComodel();
                            return new PsiReference[]{new OdooExternalIdReference(element, model, null, true)};
                        }
                    }
                }
            }
        }
        return new PsiReference[0];
    }
}
