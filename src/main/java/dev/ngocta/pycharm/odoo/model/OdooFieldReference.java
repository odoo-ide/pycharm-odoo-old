package dev.ngocta.pycharm.odoo.model;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class OdooFieldReference extends PsiReferenceBase<PsiElement> {
    private final Computable<OdooModelClass> myModelClassResolver;
    private OdooFieldPathReferences myFieldPathReferences;
    private final TypeEvalContext myContext;

    public OdooFieldReference(@NotNull PsiElement element,
                              @Nullable Computable<OdooModelClass> modelClassResolver) {
        this(element, null, modelClassResolver);
    }

    public OdooFieldReference(@NotNull PsiElement element,
                              @NotNull TextRange rangeInElement,
                              @NotNull OdooFieldPathReferences fieldPathReferences) {
        this(element, rangeInElement, fieldPathReferences.getModelClassResolver());
        myFieldPathReferences = fieldPathReferences;
    }

    private OdooFieldReference(@NotNull PsiElement element,
                               @Nullable TextRange rangeInElement,
                               @Nullable Computable<OdooModelClass> modelClassResolver) {
        super(element, rangeInElement);
        myModelClassResolver = modelClassResolver;
        myContext = TypeEvalContext.codeAnalysis(element.getProject(), element.getContainingFile());
    }

    @Nullable
    private OdooModelClass getRootModelClass() {
        if (myModelClassResolver == null) {
            return null;
        }
        return PyUtil.getNullableParameterizedCachedValue(getElement(), null, param -> {
            return myModelClassResolver.compute();
        });
    }

    private OdooModelClass getModelClass() {
        OdooModelClass rootModelClass = getRootModelClass();
        if (rootModelClass == null) {
            return null;
        }
        if (myFieldPathReferences == null) {
            return rootModelClass;
        }
        int idx = Arrays.asList(myFieldPathReferences.getReferences()).indexOf(this);
        if (idx == 0) {
            return rootModelClass;
        }
        String[] fieldNames = Arrays.copyOfRange(myFieldPathReferences.getFieldNames(), 0, idx);
        PyTargetExpression field = rootModelClass.findFieldByPath(fieldNames, myContext);
        if (field != null) {
            PyType type = OdooFieldInfo.getFieldType(field, myContext);
            if (type instanceof OdooModelClassType) {
                return ((OdooModelClassType) type).getPyClass();
            }
        }
        return null;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        return PyUtil.getNullableParameterizedCachedValue(getElement(), getRangeInElement(), param -> {
            OdooModelClass cls = getModelClass();
            if (cls != null) {
                return cls.findField(getValue(), myContext);
            }
            return null;
        });
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        OdooModelClass cls = getModelClass();
        if (cls == null) {
            return new Object[0];
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
        return getModelClass() == null;
    }
}
