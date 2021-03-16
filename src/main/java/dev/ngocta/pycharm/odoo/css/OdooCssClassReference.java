package dev.ngocta.pycharm.odoo.css;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.css.impl.stubs.index.CssClassIndex;
import com.intellij.psi.css.impl.stubs.index.CssIndexUtil;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.PlatformIcons;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class OdooCssClassReference extends PsiReferenceBase.Poly<PsiElement> {
    public OdooCssClassReference(PsiElement psiElement,
                                 TextRange range,
                                 boolean soft) {
        super(psiElement, range, soft);
    }

    @Override
    public ResolveResult @NotNull [] multiResolve(boolean incompleteCode) {
        return PyUtil.getParameterizedCachedValue(getElement(), null, param -> {
            List<PsiElement> elements = new LinkedList<>();
            GlobalSearchScope scope = OdooModuleUtils.getOdooModuleWithDependenciesOrSystemWideModulesScope(getElement());
            CssIndexUtil.processClasses(getValue(), getElement().getProject(), scope, (s, cssSelectorSuffix) -> {
                elements.add(cssSelectorSuffix);
                return true;
            });
            return PsiElementResolveResult.createResults(elements);
        });
    }

    @Override
    public Object @NotNull [] getVariants() {
        List<LookupElement> lookupElements = new LinkedList<>();
        StubIndex.getInstance().processAllKeys(CssClassIndex.KEY, getElement().getProject(), (s) -> {
            lookupElements.add(LookupElementBuilder.create(s).withIcon(PlatformIcons.CLASS_ICON));
            return true;
        });
        return lookupElements.toArray();
    }
}
