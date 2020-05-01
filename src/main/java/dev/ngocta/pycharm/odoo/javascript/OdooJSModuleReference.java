package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.javascript.JSModuleBaseReference;
import com.intellij.lang.javascript.psi.JSFunctionExpression;
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
        JSFunctionExpression func = OdooJSModuleIndex.findModuleDefineFunction(myModuleName, myElement);
        if (func != null) {
            return PsiElementResolveResult.createResults(func);
        }
        return new ResolveResult[0];
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return OdooJSModuleIndex.getAvailableModuleNames(myElement).toArray();
    }
}
