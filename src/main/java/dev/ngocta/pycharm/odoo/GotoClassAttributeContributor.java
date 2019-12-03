package dev.ngocta.pycharm.odoo;

import com.intellij.navigation.ChooseByNameContributorEx;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FindSymbolParameters;
import com.intellij.util.indexing.IdFilter;
import com.jetbrains.python.psi.stubs.PyClassAttributesIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GotoClassAttributeContributor implements ChooseByNameContributorEx {
    @Override
    public void processNames(@NotNull Processor<String> processor, @NotNull GlobalSearchScope scope, @Nullable IdFilter filter) {
        StubIndex stubIndex = StubIndex.getInstance();
        stubIndex.processAllKeys(PyClassAttributesIndex.KEY, processor, scope, filter);
    }

    @Override
    public void processElementsWithName(@NotNull String name, @NotNull Processor<NavigationItem> processor, @NotNull FindSymbolParameters parameters) {
        PyClassAttributesIndex.findClassAndInstanceAttributes(name, parameters.getProject(), parameters.getSearchScope()).forEach(processor::process);
    }
}
