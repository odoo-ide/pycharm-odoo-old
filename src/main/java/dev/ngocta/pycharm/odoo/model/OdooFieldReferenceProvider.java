package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class OdooFieldReferenceProvider extends PsiReferenceProvider {
    public static final Key<Boolean> ENABLE_SUB_FIELD = new Key<>("enableSubField");
    public static final Key<Computable<OdooModelClass>> MODEL_CLASS_RESOLVER = new Key<>("modelClassResolver");

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                 @NotNull ProcessingContext context) {
        Computable<OdooModelClass> modelClassResolver = context.get(MODEL_CLASS_RESOLVER);
        Boolean enableSubField = context.get(ENABLE_SUB_FIELD);
        if (enableSubField != null && enableSubField) {
            OdooFieldPathReferences fieldPathReferences = OdooFieldPathReferences.create(element, modelClassResolver);
            return fieldPathReferences.getReferences();
        } else {
            return new PsiReference[]{new OdooFieldReference(element, modelClassResolver)};
        }
    }
}
