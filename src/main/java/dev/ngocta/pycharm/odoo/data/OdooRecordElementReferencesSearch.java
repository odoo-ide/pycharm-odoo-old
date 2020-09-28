package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.application.QueryExecutorBase;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;

public class OdooRecordElementReferencesSearch extends QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters> {

    @Override
    public void processQuery(@NotNull ReferencesSearch.SearchParameters queryParameters,
                             @NotNull Processor<? super PsiReference> consumer) {
    }
}
