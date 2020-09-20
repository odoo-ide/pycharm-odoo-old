package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class OdooJSFieldWidgetReference extends PsiReferenceBase<PsiElement> {
    public OdooJSFieldWidgetReference(@NotNull PsiElement element) {
        super(element);
    }

    @Override
    @Nullable
    public PsiElement resolve() {
        List<PsiElement> definitions = OdooJSFieldWidgetIndex.getWidgetDefinitionsByName(getValue(), getElement());
        if (!definitions.isEmpty()) {
            return definitions.get(0);
        }
        return null;
    }

    @Override
    @NotNull
    public Object[] getVariants() {
        List<String> widgets = OdooJSFieldWidgetIndex.getAvailableWidgets(getElement());
        List<LookupElement> lookupElements = new LinkedList<>();
        for (String widget : widgets) {
            if (widget.contains(".")) {
                String[] splits = widget.split("\\.", 2);
                widget = splits[1];
            }
            lookupElements.add(LookupElementBuilder.create(widget).withIcon(AllIcons.Nodes.MultipleTypeDefinitions));
        }
        return lookupElements.toArray();
    }
}
