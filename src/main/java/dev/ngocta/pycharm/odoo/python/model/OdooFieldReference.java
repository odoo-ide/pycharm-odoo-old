package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class OdooFieldReference extends PsiReferenceBase.Poly<PsiElement> {
    private final OdooModelClass myModelClass;
    private final Computable<OdooModelClass> myModelClassResolver;
    private final OdooFieldPathReferences myFieldPathReferences;
    private final TypeEvalContext myContext;

    public OdooFieldReference(@NotNull PsiElement element,
                              @Nullable OdooModelClass modelClass) {
        this(element, null, modelClass, null, null);
    }

    public OdooFieldReference(@NotNull PsiElement element,
                              @NotNull TextRange rangeInElement,
                              @NotNull OdooFieldPathReferences fieldPathReferences) {
        this(element, rangeInElement, fieldPathReferences.getModelClass(), fieldPathReferences.getModelClassResolver(), fieldPathReferences);
    }

    public OdooFieldReference(@NotNull PsiElement element,
                              @Nullable TextRange rangeInElement,
                              @Nullable OdooModelClass modelClass,
                              @Nullable Computable<OdooModelClass> modelClassResolver,
                              @Nullable OdooFieldPathReferences fieldPathReferences) {
        super(element, rangeInElement, false);
        myModelClass = modelClass;
        myModelClassResolver = modelClassResolver;
        myFieldPathReferences = fieldPathReferences;
        myContext = TypeEvalContext.codeAnalysis(getProject(), element.getContainingFile());
    }

    public Project getProject() {
        return getElement().getProject();
    }

    private OdooModelClass getModelClass() {
        return PyUtil.getNullableParameterizedCachedValue(getElement(), getRangeInElement(), param -> {
            OdooModelClass cls = myModelClass;
            if (cls == null && myModelClassResolver != null) {
                cls = myModelClassResolver.compute();
            }
            if (myFieldPathReferences == null) {
                return cls;
            }
            int idx = Arrays.asList(myFieldPathReferences.getReferences()).indexOf(this);
            if (idx == 0) {
                return cls;
            }
            PsiReference prevReference = myFieldPathReferences.getReferences()[idx - 1];
            PsiElement field = prevReference.resolve();
            if (field instanceof PyTargetExpression) {
                PyType type = OdooFieldInfo.getFieldType(field, myContext);
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
            PsiElement field = cls.findField(getValue(), myContext);
            return field != null ? PsiElementResolveResult.createResults(field) : ResolveResult.EMPTY_ARRAY;
        }
        Collection<PsiElement> implicitFields = resolveImplicitFields();
        return PsiElementResolveResult.createResults(implicitFields);
    }

    @NotNull
    protected Collection<PsiElement> resolveImplicitFields() {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(getElement());
        if (module != null) {
            Collection<PsiElement> fields = OdooModelUtils.findFields(getValue(), getElement());
            return OdooModuleUtils.sortElementByOdooModuleDependOrder(fields);
        }
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        OdooModelClass cls = getModelClass();
        if (cls != null) {
            return OdooModelUtils.getFieldLookupElements(cls, myContext);
        }
        return OdooModelUtils.getImplicitFieldLookupElements(getElement());
    }

    @Override
    public boolean isSoft() {
        OdooModelClass cls = getModelClass();
        if (cls == null) {
            return true;
        }
        if (myFieldPathReferences != null) {
            String[] fieldNames = myFieldPathReferences.getFieldNames();
            return fieldNames.length > 1 && "parent".equals(fieldNames[0]);
        }
        return OdooNames.IR_RULE.equals(cls.getName()) && OdooNames.IR_RULE_FIELD_GLOBAL.equals(getValue());
    }
}
