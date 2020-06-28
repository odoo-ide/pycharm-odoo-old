package dev.ngocta.pycharm.odoo;

import com.intellij.codeInsight.completion.CompletionUtilCoreImpl;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ArrayUtil;
import com.intellij.util.Processor;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.impl.references.PyQualifiedReference;
import com.jetbrains.python.psi.resolve.ImplicitResolveResult;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import com.jetbrains.python.psi.stubs.PyClassAttributesIndex;
import com.jetbrains.python.psi.stubs.PyFunctionNameIndex;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.model.OdooFieldIndex;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import dev.ngocta.pycharm.odoo.module.OdooModuleUtils;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class OdooPyQualifiedReference extends PyQualifiedReference {
    private static final int MIN_LEN_VARIANT = 3;

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
        if (!OdooPyUtils.isWeakType(qualifierType) || (element.getName() != null && element.getName().length() <= MIN_LEN_VARIANT)) {
            return variants;
        }

        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(element);
        if (odooModule == null) {
            return variants;
        }

        Set<String> knownNames = new THashSet<>();
        for (Object variant : variants) {
            if (variant instanceof LookupElement) {
                knownNames.add(((LookupElement) variant).getLookupString());
            }
        }

        List<Object> extraVariants = new LinkedList<>();
        Processor<String> processor = s -> {
            if (!s.startsWith("__") && s.length() > MIN_LEN_VARIANT && !knownNames.contains(s)) {
                extraVariants.add(s);
                knownNames.add(s);
            }
            return true;
        };

        GlobalSearchScope scope = odooModule.getSearchScope();

        FileBasedIndex fileBasedIndex = FileBasedIndex.getInstance();
        fileBasedIndex.processAllKeys(OdooFieldIndex.NAME, processor, scope, null);

        StubIndex stubIndex = StubIndex.getInstance();
        stubIndex.processAllKeys(PyClassAttributesIndex.KEY, processor, scope, null);
        stubIndex.processAllKeys(PyFunctionNameIndex.KEY, processor, scope, null);

        return ArrayUtil.mergeArrays(variants, extraVariants.toArray());
    }
}
