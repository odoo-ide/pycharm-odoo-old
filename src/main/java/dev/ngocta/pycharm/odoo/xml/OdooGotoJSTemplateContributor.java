package dev.ngocta.pycharm.odoo.xml;

import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import dev.ngocta.pycharm.odoo.xml.dom.js.OdooDomJSTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooGotoJSTemplateContributor implements ChooseByNameContributorEx {
    @Override
    public void processNames(@NotNull Processor<? super String> processor,
                             @NotNull GlobalSearchScope scope,
                             @Nullable IdFilter filter) {
        if (scope.getProject() != null) {
            OdooJSTemplateIndex.processAvailableTemplateNames(scope, scope.getProject(), processor::process);
        }
    }

    @Override
    public void processElementsWithName(@NotNull String name,
                                        @NotNull Processor<? super NavigationItem> processor,
                                        @NotNull FindSymbolParameters parameters) {
        List<OdooDomJSTemplate> templates = OdooJSTemplateIndex.findTemplatesByName(name, parameters.getSearchScope(), parameters.getProject());
        for (OdooDomJSTemplate template : templates) {
            OdooJSTemplateElement element = template.getNavigationElement();
            if (element != null) {
                processor.process(element);
            }
        }
    }
}
