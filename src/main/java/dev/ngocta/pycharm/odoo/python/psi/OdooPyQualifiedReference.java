package dev.ngocta.pycharm.odoo.python.psi;

import com.google.common.collect.Streams;
import com.intellij.codeInsight.completion.CompletionUtilCoreImpl;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.references.PyQualifiedReference;
import com.jetbrains.python.psi.resolve.ImplicitResolveResult;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import com.jetbrains.python.psi.stubs.PyClassAttributesIndex;
import com.jetbrains.python.psi.stubs.PyFunctionNameIndex;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.jetbrains.python.pyi.PyiUtil;
import dev.ngocta.pycharm.odoo.OdooUtils;
import dev.ngocta.pycharm.odoo.python.OdooPyUtils;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClass;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
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
        if (results.size() == 1) {
            return results;
        }

        if (results.isEmpty() && myElement.getName() != null && myContext.allowImplicits()) {
            PyExpression qualifier = myElement.getQualifier();
            if (qualifier != null) {
                PyType qualifierType = myContext.getTypeEvalContext().getType(qualifier);
                if (OdooPyUtils.isUnknownType(qualifierType)) {
                    Project project = myElement.getProject();
                    GlobalSearchScope scope = OdooUtils.getProjectModuleWithDependenciesScope(myElement);
                    Collection<PyTargetExpression> attrs = PyClassAttributesIndex.findClassAndInstanceAttributes(myElement.getName(), project, scope);
                    Collection<PyFunction> functions = PyFunctionNameIndex.find(myElement.getName(), project, scope);
                    Streams.concat(attrs.stream(), functions.stream()).forEach(e -> {
                        results.add(new ImplicitResolveResult(e, RatedResolveResult.RATE_NORMAL));
                    });
                }
            }
        }

        if (results.isEmpty()) {
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

        // Remove duplicate variants
        if (qualifierType instanceof OdooModelClassType) {
            variants = Arrays.stream(variants).filter(variant -> {
                if (variant instanceof LookupElement) {
                    PsiElement e = ((LookupElement) variant).getPsiElement();
                    if (e != null) {
                        return ScopeUtil.getScopeOwner(e) instanceof PyClass;
                    }
                }
                return true;
            }).toArray();
            return variants;
        }

        if (!OdooPyUtils.isUnknownType(qualifierType)) {
            return variants;
        }

        List<Object> extendedVariants = new LinkedList<>();
        for (Object variant : variants) {
            if (variant instanceof LookupElement) {
                String name = ((LookupElement) variant).getLookupString();
                addExtendedVariant(name, extendedVariants);
            }
        }

        GlobalSearchScope scope = GlobalSearchScope.projectScope(element.getProject());
        StubIndex.getInstance().processAllKeys(PyClassAttributesIndex.KEY, s -> {
            addExtendedVariant(s, extendedVariants);
            return true;
        }, scope, null);

        return extendedVariants.toArray();
    }

    private void addExtendedVariant(@NotNull String name,
                                    @NotNull Collection<Object> result) {
        result.add(LookupElementBuilder.create(name));
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        if (isReferenceToSameOdooModelAttribute(element)) {
            return true;
        }
        return super.isReferenceTo(element);
    }

    protected boolean isReferenceToSameOdooModelAttribute(@NotNull PsiElement element) {
        if (element instanceof PyFunction || PyUtil.isClassAttribute(element)) {
            OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(element);
            if (modelClass != null) {
                ResolveResult[] results = multiResolve(true);
                for (ResolveResult result : results) {
                    OdooModelClass modelClassOfResult = OdooModelUtils.getContainingOdooModelClass(result.getElement());
                    if (modelClass.equals(modelClassOfResult)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
