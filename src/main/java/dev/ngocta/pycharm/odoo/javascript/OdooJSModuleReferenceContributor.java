package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.DialectDetector;
import com.intellij.lang.javascript.frameworks.modules.JSBaseModuleReferenceContributor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooJSModuleReferenceContributor extends JSBaseModuleReferenceContributor {
    @Override
    public boolean isApplicable(@NotNull PsiElement psiElement) {
        return DialectDetector.isJavaScript(psiElement) && OdooModuleUtils.isInOdooModule(psiElement);
    }

    @NotNull
    @Override
    protected PsiReference[] getReferences(@NotNull String text,
                                           @NotNull PsiElement host,
                                           int offset,
                                           @Nullable PsiReferenceProvider psiReferenceProvider,
                                           boolean isCommonJSModule) {
        return new PsiReference[]{new OdooJSModuleReference(host, text)};
    }
}
