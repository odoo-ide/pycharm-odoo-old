package dev.ngocta.pycharm.odoo.module;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModuleReference extends PsiReferenceBase<PsiElement> {
    public OdooModuleReference(@NotNull PsiElement element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return OdooModuleIndex.getModule(getValue(), getElement().getProject());
    }
}
