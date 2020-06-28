package dev.ngocta.pycharm.odoo.model;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import dev.ngocta.pycharm.odoo.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooFieldReference extends PsiReferenceBase.Poly<PsiElement> {
    private final OdooModelClass myModelClass;
    private OdooFieldPathReferences myFieldPathReferences;
    private final TypeEvalContext myContext;

    public OdooFieldReference(@NotNull PsiElement element,
                              @Nullable OdooModelClass modelClass) {
        this(element, null, modelClass);
    }

    public OdooFieldReference(@NotNull PsiElement element,
                              @NotNull TextRange rangeInElement,
                              @NotNull OdooFieldPathReferences fieldPathReferences) {
        this(element, rangeInElement, fieldPathReferences.getModelClass());
        myFieldPathReferences = fieldPathReferences;
    }

    private OdooFieldReference(@NotNull PsiElement element,
                               @Nullable TextRange rangeInElement,
                               @Nullable OdooModelClass modelClass) {
        super(element, rangeInElement, false);
        myModelClass = modelClass;
        myContext = TypeEvalContext.codeAnalysis(getProject(), element.getContainingFile());
    }

    public Project getProject() {
        return getElement().getProject();
    }

    private OdooModelClass getModelClass() {
        if (myFieldPathReferences == null) {
            return myModelClass;
        }
        int idx = Arrays.asList(myFieldPathReferences.getReferences()).indexOf(this);
        if (idx == 0) {
            return myModelClass;
        }
        return PyUtil.getNullableParameterizedCachedValue(getElement(), getRangeInElement(), param -> {
            PsiReference prevReference = myFieldPathReferences.getReferences()[idx - 1];
            PsiElement field = prevReference.resolve();
            if (field instanceof PyTargetExpression) {
                PyType type = OdooFieldInfo.getFieldType((PyTargetExpression) field, myContext);
                if (type instanceof OdooModelClassType) {
                    return ((OdooModelClassType) type).getPyClass();
                }
            }
            String prevName = myFieldPathReferences.getFieldNames()[idx - 1];
            PyType type = OdooModelUtils.guessFieldTypeByName(prevName, getElement(), myContext);
            if (type instanceof OdooModelClassType) {
                return ((OdooModelClassType) type).getPyClass();
            }
            return null;
        });
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return PyUtil.getParameterizedCachedValue(getElement(), getRangeInElement(), param -> {
            return multiResolveInner();
        });
    }

    protected ResolveResult[] multiResolveInner() {
        OdooModelClass cls = getModelClass();
        if (cls != null) {
            PyTargetExpression field = cls.findField(getValue(), myContext);
            return field != null ? PsiElementResolveResult.createResults(field) : ResolveResult.EMPTY_ARRAY;
        }
        Collection<PyTargetExpression> implicitFields = resolveImplicitFields();
        return PsiElementResolveResult.createResults(implicitFields);
    }

    @NotNull
    protected Collection<PyTargetExpression> resolveImplicitFields() {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(getElement());
        if (module != null) {
            Collection<PyTargetExpression> fields = OdooModelUtils.findFields(getValue(), getElement());
            return OdooModuleUtils.sortElementByOdooModuleDependOrder(fields);
        }
        return Collections.emptyList();
    }

    @NotNull
    protected Collection<String> getImplicitVariants() {
        return OdooFieldIndex.getAvailableFieldNames(getElement());
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        OdooModelClass cls = getModelClass();
        if (cls == null) {
            return getImplicitVariants().toArray();
        }
        Map<String, LookupElement> elements = new LinkedHashMap<>();
        cls.visitField(field -> {
            if (field.getName() != null) {
                LookupElement element = OdooModelUtils.createCompletionLine(field, myContext);
                elements.putIfAbsent(field.getName(), element);
            }
            return true;
        }, myContext);
        return elements.values().toArray();
    }

    @Override
    public boolean isSoft() {
        OdooModelClass cls = getModelClass();
        if (cls == null) {
            return true;
        }
        if (OdooNames.IR_RULE.equals(cls.getName()) && OdooNames.IR_RULE_GLOBAL.equals(getValue())) {
            return true;
        }
        return false;
    }
}
