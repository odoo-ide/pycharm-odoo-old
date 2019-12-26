package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class OdooExternalIdReferenceProvider extends PsiReferenceProvider {
    public static final Key<String> ACCEPTED_MODEL = new Key<>("acceptedModel");

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String acceptedModel = context.get(ACCEPTED_MODEL);
        if (acceptedModel != null) {
            return new PsiReference[]{new OdooExternalIdReference(element, acceptedModel)};
        }
        return new PsiReference[]{new OdooExternalIdReference(element)};
    }
}
