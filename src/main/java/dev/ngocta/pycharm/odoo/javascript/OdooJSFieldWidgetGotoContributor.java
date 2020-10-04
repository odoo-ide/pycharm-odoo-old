package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class OdooJSFieldWidgetGotoContributor implements ChooseByNameContributorEx {
    @Override
    public void processNames(@NotNull Processor<? super String> processor,
                             @NotNull GlobalSearchScope scope,
                             @Nullable IdFilter filter) {
        if (scope.getProject() != null) {
            Collection<String> widgets = OdooJSFieldWidgetIndex.getAvailableWidgetNames(scope, scope.getProject(), true);
            for (String widget : widgets) {
                processor.process(widget);
            }
        }
    }

    @Override
    public void processElementsWithName(@NotNull String name,
                                        @NotNull Processor<? super NavigationItem> processor,
                                        @NotNull FindSymbolParameters parameters) {
        Collection<OdooJSFieldWidget> elements = OdooJSFieldWidgetIndex.getWidgetsByName(
                name, parameters.getSearchScope(), parameters.getProject(), true);
        elements.forEach(processor::process);
    }
}
