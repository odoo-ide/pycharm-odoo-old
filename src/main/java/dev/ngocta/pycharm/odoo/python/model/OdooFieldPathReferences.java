package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class OdooFieldPathReferences {
    private final PsiElement myElement;
    private final OdooModelClass myModelClass;
    private final Computable<OdooModelClass> myModelClassResolver;
    private PsiReference[] myReferences;
    private String[] myFieldNames;

    private OdooFieldPathReferences(@NotNull PsiElement element,
                                    @Nullable OdooModelClass modelClass,
                                    @Nullable Computable<OdooModelClass> modelClassResolver) {
        myElement = element;
        myModelClass = modelClass;
        myModelClassResolver = modelClassResolver;
    }

    public static OdooFieldPathReferences create(@NotNull PsiElement element,
                                                 @Nullable OdooModelClass modelClass,
                                                 @Nullable Computable<OdooModelClass> modelClassResolver) {
        OdooFieldPathReferences fieldPathReferences = new OdooFieldPathReferences(element, modelClass, modelClassResolver);
        List<PsiReference> references = new LinkedList<>();
        TextRange range = ElementManipulators.getValueTextRange(element);
        String rangeValue = range.substring(element.getText());
        String[] fieldNames = rangeValue.split("\\.");
        int idx = range.getStartOffset();
        for (String name : fieldNames) {
            TextRange subRange = TextRange.from(idx, name.length());
            references.add(new OdooFieldReference(element, subRange, fieldPathReferences));
            idx = subRange.getEndOffset() + 1;
        }
        fieldPathReferences.myFieldNames = fieldNames;
        fieldPathReferences.myReferences = references.toArray(PsiReference.EMPTY_ARRAY);
        return fieldPathReferences;
    }

    public String[] getFieldNames() {
        return myFieldNames;
    }

    public PsiReference[] getReferences() {
        return myReferences;
    }

    public OdooModelClass getModelClass() {
        return myModelClass;
    }

    public Computable<OdooModelClass> getModelClassResolver() {
        return myModelClassResolver;
    }

    public PsiElement getElement() {
        return myElement;
    }
}
