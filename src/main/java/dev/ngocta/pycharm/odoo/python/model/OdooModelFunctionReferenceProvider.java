package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class OdooModelFunctionReferenceProvider extends PsiReferenceProvider {
    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                           @NotNull ProcessingContext context) {
        OdooModelClass cls = OdooModelUtils.getContainingOdooModelClass(element);
        if (cls != null) {
            return new PsiReference[]{new OdooModelFunctionReference(element, cls)};
        } else {
            return new PsiReference[0];
        }
    }
}
