package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.model.OdooModelFunctionReference;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface OdooDomViewButton extends OdooDomModelScopedViewElement {
    @Attribute("name")
    @Referencing(ButtonNameReferencing.class)
    GenericAttributeValue<String> getName();

    @Attribute("type")
    GenericAttributeValue<String> getType();

    class ButtonNameReferencing implements CustomReferenceConverter<String> {
        @NotNull
        @Override
        public PsiReference[] createReferences(GenericDomValue<String> value,
                                               PsiElement element,
                                               ConvertContext context) {
            String name = value.getStringValue();
            OdooDomViewButton button = value.getParentOfType(OdooDomViewButton.class, true);
            if (name != null && button != null) {
                String type = button.getType().getStringValue();
                if ("object".equals(type) || (type == null && !name.contains("%"))) {
                    String model = button.getModel();
                    if (model != null) {
                        return new PsiReference[]{new OdooModelFunctionReference(element, model)};
                    }
                } else if ("action".equals(type) || (type == null && name.contains("%"))) {
                    Matcher matcher = Pattern.compile("(?<=%\\()(\\w+\\.)?\\w+").matcher(name);
                    if (matcher.find()) {
                        TextRange range = ElementManipulators.getValueTextRange(element);
                        range = range.cutOut(new TextRange(matcher.start(), matcher.end()));
                        return new PsiReference[]{new OdooExternalIdReference(element, range, OdooNames.ACTION_MODELS, null, true)};
                    }
                }
            }
            return new PsiReference[0];
        }
    }
}
