package dev.ngocta.pycharm.odoo.module;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.jetbrains.python.psi.PyUtil;
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
        return PyUtil.getNullableParameterizedCachedValue(getElement(), null, param -> {
            OdooModule module = OdooModuleIndex.getOdooModuleByName(getValue(), getElement());
            return module != null ? module.getDirectory() : null;
        });
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        Collection<OdooModule> modules = OdooModuleIndex.getAvailableOdooModules(getElement());
        OdooModule module = OdooModuleUtils.getContainingOdooModule(getElement());
        if (module != null) {
            modules.remove(module);
        }
        return modules.stream().map(OdooModule::getDirectory).toArray();
    }
}
