package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class OdooFieldReferenceProvider extends PsiReferenceProvider {
    public static Key<OdooModelClass> MODEL_CLASS = new Key<>("modelClass");
    public static Key<Boolean> ENABLE_SUB_FIELD = new Key<>("enableSubField");

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        OdooModelClass cls = context.get(MODEL_CLASS);
        Boolean enableSubField = context.get(ENABLE_SUB_FIELD);
        if (cls == null) {
            return new PsiReference[0];
        }
        if (enableSubField != null && enableSubField) {
            OdooFieldPathReferences fieldPathReferences = OdooFieldPathReferences.create(element, cls);
            return fieldPathReferences.getReferences();
        } else {
            return new PsiReference[]{new OdooFieldReference(element, cls)};
        }
    }
}
