package dev.ngocta.pycharm.odoo.module;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class OdooModuleReference extends PsiReferenceBase<PsiElement> {
    public OdooModuleReference(@NotNull PsiElement element) {
        super(element);
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return OdooModuleIndex.getModule(getValue(), getElement().getProject());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        Collection<PsiDirectory> modules = OdooModuleIndex.getAllModules(getElement().getProject());
        PsiDirectory module = OdooUtils.getOdooModule(getElement());
        if (module != null) {
            modules.remove(module);
        }
        return modules.toArray();
    }
}
