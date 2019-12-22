package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class OdooFieldReferenceSet {
    private final PsiElement myElement;
    private final OdooModelClass myModelClass;
    private PsiReference[] myReferences;
    private String[] myFieldNames;

    private OdooFieldReferenceSet(@NotNull PsiElement element, @NotNull OdooModelClass modelClass) {
        myElement = element;
        myModelClass = modelClass;
    }

    public static OdooFieldReferenceSet create(@NotNull PsiElement element, @NotNull OdooModelClass modelClass) {
        OdooFieldReferenceSet referenceSet = new OdooFieldReferenceSet(element, modelClass);
        List<PsiReference> references = new LinkedList<>();
        TextRange range = ElementManipulators.getValueTextRange(element);
        String rangeValue = range.substring(element.getText());
        String[] fieldNames = rangeValue.split("\\.");
        int idx = range.getStartOffset();
        for (String name : fieldNames) {
            TextRange subRange = TextRange.from(idx, name.length());
            references.add(new OdooFieldReference(element, subRange, referenceSet));
            idx = subRange.getEndOffset() + 1;
        }
        referenceSet.myFieldNames = fieldNames;
        referenceSet.myReferences = references.toArray(PsiReference.EMPTY_ARRAY);
        return referenceSet;
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

    public PsiElement getElement() {
        return myElement;
    }
}
