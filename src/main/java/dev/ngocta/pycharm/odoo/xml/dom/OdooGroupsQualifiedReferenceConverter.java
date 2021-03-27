package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.util.xml.ConvertContext;
import com.intellij.util.xml.CustomReferenceConverter;
import com.intellij.util.xml.GenericDomValue;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdReferenceProvider;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilters;
import org.jetbrains.annotations.NotNull;

public class OdooGroupsQualifiedReferenceConverter implements CustomReferenceConverter<String> {
    @Override
    public PsiReference @NotNull [] createReferences(GenericDomValue<String> value,
                                                     PsiElement element,
                                                     ConvertContext context) {
        return OdooExternalIdReferenceProvider.getCommaSeparatedReferences(element, OdooRecordFilters.RES_GROUPS, false);
    }
}
