package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.frameworks.modules.JSBaseModuleReferenceContributor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooJSModuleReferenceContributor extends JSBaseModuleReferenceContributor {
    @Override
    public boolean isApplicable(@NotNull PsiElement psiElement) {
        return OdooJSUtils.isInOdooJSModule(psiElement);
    }

    @Override
    protected PsiReference @NotNull [] getReferences(@NotNull String text,
                                                     @NotNull PsiElement host,
                                                     int offset,
                                                     @Nullable PsiReferenceProvider psiReferenceProvider,
                                                     boolean isCommonJSModule) {
        return new PsiReference[]{new OdooJSModuleReference(host, text)};
    }
}
