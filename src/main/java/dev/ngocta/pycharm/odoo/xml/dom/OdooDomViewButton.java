package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.*;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdReference;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilters;
import dev.ngocta.pycharm.odoo.python.model.OdooModelFunctionPublicReference;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface OdooDomViewButton extends OdooDomModelScopedViewElement {
    @Attribute("name")
    @Referencing(ButtonNameReferencing.class)
    GenericAttributeValue<String> getNameAttribute();

    @Attribute("type")
    GenericAttributeValue<String> getTypeAttribute();

    class ButtonNameReferencing implements CustomReferenceConverter<String> {
        @Override
        public PsiReference @NotNull [] createReferences(GenericDomValue<String> value,
                                                         PsiElement element,
                                                         ConvertContext context) {
            String name = value.getStringValue();
            OdooDomViewButton button = value.getParentOfType(OdooDomViewButton.class, true);
            if (name != null && button != null) {
                String type = button.getTypeAttribute().getStringValue();
                if ("object".equals(type) || (type == null && !name.contains("%"))) {
                    String model = button.getModel();
                    if (model != null) {
                        return new PsiReference[]{new OdooModelFunctionPublicReference(element, model)};
                    }
                } else if ("action".equals(type) || (type == null && name.contains("%"))) {
                    Matcher matcher = Pattern.compile("(?<=%\\()(\\w+\\.)?\\w+").matcher(name);
                    if (matcher.find()) {
                        TextRange range = ElementManipulators.getValueTextRange(element);
                        range = range.cutOut(new TextRange(matcher.start(), matcher.end()));
                        return new PsiReference[]{
                                new OdooExternalIdReference(element, range, OdooRecordFilters.ACTION_MODELS, true)
                        };
                    }
                }
            }
            return new PsiReference[0];
        }
    }
}
