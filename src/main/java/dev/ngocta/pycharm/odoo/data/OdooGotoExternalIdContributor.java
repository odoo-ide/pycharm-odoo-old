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
    public void processNames(@NotNull Processor<? super String> processor,
                             @NotNull GlobalSearchScope scope,
                             @Nullable IdFilter filter) {
        Project project = scope.getProject();
        if (project != null) {
            Collection<String> ids = OdooExternalIdIndex.getAllIds(project, scope);
            ids.forEach(processor::process);
        }
    }

    @Override
    public void processElementsWithName(@NotNull String name,
                                        @NotNull Processor<? super NavigationItem> processor,
                                        @NotNull FindSymbolParameters parameters) {
        Project project = parameters.getProject();
        Collection<OdooRecord> records = OdooExternalIdIndex.findRecordsByQualifiedId(name, project, parameters.getSearchScope());
        records.forEach(record -> {
            record.getNavigationElements(project).forEach(processor::process);
        });
    }
}
