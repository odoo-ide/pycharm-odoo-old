package dev.ngocta.pycharm.odoo.data;

import com.google.common.collect.ImmutableMap;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.model.OdooModelReference;
import org.jetbrains.annotations.NotNull;

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
        }
        return new PsiReference[0];
    }
}
