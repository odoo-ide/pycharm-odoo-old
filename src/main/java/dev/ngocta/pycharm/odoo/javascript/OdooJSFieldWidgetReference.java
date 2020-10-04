package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomViewElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class OdooJSFieldWidgetReference extends PsiReferenceBase.Poly<PsiElement> {
    public OdooJSFieldWidgetReference(@NotNull PsiElement element) {
        super(element);
    }

    @Nullable
    private String getViewType() {
        DomElement domElement = DomUtil.getDomElement(getElement());
        if (domElement instanceof OdooDomViewElement) {
            return ((OdooDomViewElement) domElement).getViewType();
        }
        return null;
    }

    @Override
    public @NotNull ResolveResult[] multiResolve(boolean incompleteCode) {
        return PyUtil.getParameterizedCachedValue(getElement(), getValue(), param -> {
            Collection<OdooJSFieldWidget> widgets = OdooJSFieldWidgetIndex.getWidgetsByName(getValue(), getElement(), false);
            String viewType = getViewType();
            if (viewType != null) {
                widgets.removeIf(widget -> widget.getViewType() != null && !widget.getViewType().equals(viewType));
            }
            return PsiElementResolveResult.createResults(widgets);
        });
    }

    @Override
    @NotNull
    public Object[] getVariants() {
        Collection<String> names = OdooJSFieldWidgetIndex.getAvailableWidgetNames(getElement(), false);
        List<LookupElement> lookupElements = new LinkedList<>();
        for (String name : names) {
            lookupElements.add(LookupElementBuilder.create(name).withIcon(AllIcons.Nodes.MultipleTypeDefinitions));
        }
        return lookupElements.toArray();
    }
}
