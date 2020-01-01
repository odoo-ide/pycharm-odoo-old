package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class OdooExternalIdReferenceProvider extends PsiReferenceProvider {
    public static final Key<String> MODEL = new Key<>("model");
    public static final Key<OdooRecordSubType> SUB_TYPE = new Key<>("subType");
    public static final Key<Boolean> ALLOW_RELATIVE = new Key<>("allowRelative");

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        String acceptedModel = context.get(MODEL);
        OdooRecordSubType subType = context.get(SUB_TYPE);
        Boolean allowRelative = context.get(ALLOW_RELATIVE);
        return new PsiReference[]{new OdooExternalIdReference(element, acceptedModel, subType, Boolean.TRUE.equals(allowRelative))};
    }
}
