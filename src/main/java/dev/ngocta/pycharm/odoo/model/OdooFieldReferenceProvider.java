package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class OdooFieldReferenceProvider extends PsiReferenceProvider {
    public static Key<OdooModelClass> MODEL_CLASS = new Key<>("modelClass");

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        OdooModelClass cls = context.get(MODEL_CLASS);
        if (cls == null) {
            return new PsiReference[0];
        }
        OdooFieldReferenceSet referenceSet = OdooFieldReferenceSet.create(element, cls);
        return referenceSet.getReferences();
    }
}
