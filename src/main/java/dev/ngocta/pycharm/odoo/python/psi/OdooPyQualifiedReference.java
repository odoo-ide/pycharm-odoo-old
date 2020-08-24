package dev.ngocta.pycharm.odoo.python.psi;

import com.intellij.codeInsight.completion.CompletionUtilCoreImpl;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.references.PyQualifiedReference;
import com.jetbrains.python.psi.resolve.ImplicitResolveResult;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import com.jetbrains.python.psi.stubs.PyClassAttributesIndex;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.jetbrains.python.pyi.PyiUtil;
import dev.ngocta.pycharm.odoo.OdooUtils;
import dev.ngocta.pycharm.odoo.python.OdooPyUtils;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class OdooPyQualifiedReference extends PyQualifiedReference {
    public OdooPyQualifiedReference(PyQualifiedExpression element,
                                    PyResolveContext context) {
        super(element, context);
    }

    @NotNull
    @Override
    protected List<RatedResolveResult> resolveInner() {
        List<RatedResolveResult> results = super.resolveInner();
        if (results.size() <= 1) {
            return results;
        }
        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(getElement());
        GlobalSearchScope scope = OdooUtils.getProjectModuleWithDependenciesScope(getElement());
        Set<PsiElement> elements = results.stream()
                .map(RatedResolveResult::getElement)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        results.removeIf(result -> {
            if (!(result instanceof ImplicitResolveResult)) {
                return false;
            }
            PsiElement element = result.getElement();
            if (element == null) {
                return true;
            }
            if (element instanceof PyElement) {
                PsiElement origin = PyiUtil.getOriginalElementOrLeaveAsIs((PyElement) element, PyElement.class);
                if (!origin.equals(element) && elements.contains(origin)) {
                    return true;
                }
            }
            if (element instanceof PyTargetExpression
                    && !PyUtil.isClassAttribute(element)
                    && OdooModelUtils.isInOdooModelClass(element)) {
                return true;
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
            return file != null && !scope.contains(file.getVirtualFile());
        });
        return results;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        Object[] variants = super.getVariants();
        PyExpression qualifier = myElement.getQualifier();
        if (qualifier != null) {
            qualifier = CompletionUtilCoreImpl.getOriginalOrSelf(qualifier);
        }
        if (qualifier == null) {
            return EMPTY_ARRAY;
        }
        final PyQualifiedExpression element = CompletionUtilCoreImpl.getOriginalOrSelf(myElement);
        PyType qualifierType = TypeEvalContext.codeCompletion(element.getProject(), element.getContainingFile()).getType(qualifier);
        if (!OdooPyUtils.isUnknownType(qualifierType)) {
            return variants;
        }

        Set<String> extendedVariants = new THashSet<>();
        for (Object variant : variants) {
            if (variant instanceof LookupElement) {
                String name = ((LookupElement) variant).getLookupString();
                extendedVariants.add(name);
            }
        }

        GlobalSearchScope scope = OdooUtils.getProjectModuleWithDependenciesScope(element);
        StubIndex.getInstance().processAllKeys(PyClassAttributesIndex.KEY, s -> {
            if (s.length() > 3) {
                extendedVariants.add(s);
            }
            return true;
        }, scope, null);

        return extendedVariants.toArray();
    }
}
