package dev.ngocta.pycharm.odoo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.impl.references.PyQualifiedReference;
import com.jetbrains.python.psi.resolve.ImplicitResolveResult;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import dev.ngocta.pycharm.odoo.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OdooPyQualifiedReference extends PyQualifiedReference {
    public OdooPyQualifiedReference(PyQualifiedExpression element,
                                    PyResolveContext context) {
        super(element, context);
    }

    @NotNull
    @Override
    protected List<RatedResolveResult> resolveInner() {
        List<RatedResolveResult> results = super.resolveInner();
        if (results.size() > 1) {
            OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(getElement());
            GlobalSearchScope scope = OdooUtils.getProjectModuleAndDependenciesScope(getElement());
            results.removeIf(result -> {
                if (!(result instanceof ImplicitResolveResult)) {
                    return false;
                }
                PsiElement element = result.getElement();
                if (element == null) {
                    return false;
                }
                OdooModule targetOdooModule = OdooModuleUtils.getContainingOdooModule(element);
                if (odooModule != null && targetOdooModule != null &&
                        !odooModule.equals(targetOdooModule) && !odooModule.isDependOn(targetOdooModule)) {
                    return true;
                }
                if (odooModule == null && targetOdooModule != null) {
                    return true;
                }
                PsiFile file = element.getContainingFile();
                if (file != null && !scope.contains(file.getVirtualFile())) {
                    return true;
                }
                return false;
            });
        }
        return results;
    }
}
