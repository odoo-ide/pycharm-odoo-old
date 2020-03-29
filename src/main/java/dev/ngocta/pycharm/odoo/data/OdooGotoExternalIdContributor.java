package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class OdooGotoExternalIdContributor implements ChooseByNameContributorEx {
    @Override
    public void processNames(@NotNull Processor<String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
        Collection<String> ids = OdooExternalIdIndex.getAllIds(scope);
        ids.forEach(processor::process);
    }

    @Override
    public void processElementsWithName(@NotNull String name, @NotNull Processor<NavigationItem> processor, @NotNull FindSymbolParameters parameters) {
        Project project = parameters.getProject();
        Collection<OdooRecord> records = OdooExternalIdIndex.findRecordsById(name, parameters.getSearchScope());
        records.forEach(record -> {
            record.getNavigationItems(project).forEach(processor::process);
        });
    }
}
