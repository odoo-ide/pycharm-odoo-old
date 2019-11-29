package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class OdooComputeFunctionProvider extends PsiReferenceProvider {
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        if (element instanceof PyStringLiteralExpression) {
            PsiReference ref = new OdooComputeFunctionReference((PyStringLiteralExpression) element);
            return Collections.singletonList(ref).toArray(PsiReference.EMPTY_ARRAY);
        }
        return new PsiReference[0];
    }
}
