package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class OdooComputeFunctionReferenceProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        OdooModelClass cls = OdooModelUtils.getContainingOdooModelClass(element);
        if (cls != null) {
            return new PsiReference[]{new OdooModelFunctionReference(element, cls)};
        } else {
            return new PsiReference[0];
        }
    }
}
