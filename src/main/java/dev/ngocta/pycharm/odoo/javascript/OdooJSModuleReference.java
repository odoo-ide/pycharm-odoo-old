package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.javascript.JSModuleBaseReference;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiPolyVariantReferenceBase;
import com.intellij.psi.ResolveResult;
import org.jetbrains.annotations.NotNull;

public class OdooJSModuleReference extends PsiPolyVariantReferenceBase<PsiElement> implements JSModuleBaseReference {
    private final String myModuleName;

    public OdooJSModuleReference(@NotNull PsiElement element,
                                 @NotNull String moduleName) {
        super(element);
        myModuleName = moduleName;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        OdooJSModule module = OdooJSModuleIndex.findModule(myModuleName, myElement);
        if (module != null) {
            return PsiElementResolveResult.createResults(module);
        }
        return new ResolveResult[0];
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return OdooJSModuleIndex.getAvailableModuleNames(myElement).toArray();
    }
}
