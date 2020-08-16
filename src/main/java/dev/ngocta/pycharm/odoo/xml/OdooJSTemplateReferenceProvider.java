package dev.ngocta.pycharm.odoo.xml;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

public class OdooJSTemplateReferenceProvider extends PsiReferenceProvider {
    public static final Key<Boolean> IS_QUALIFIED = new Key<>("isQualified");

    @Override
    @NotNull
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                 @NotNull ProcessingContext context) {
        boolean isQualifier = ObjectUtils.notNull(context.get(IS_QUALIFIED), false);
        return new PsiReference[]{new OdooJSTemplateReference(element, isQualifier)};
    }
}
