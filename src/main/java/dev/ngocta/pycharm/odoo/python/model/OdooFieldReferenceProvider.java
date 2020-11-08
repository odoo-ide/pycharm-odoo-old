package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdooFieldReferenceProvider extends PsiReferenceProvider {
    public static final Key<Boolean> ENABLE_SUB_FIELD = new Key<>("enableSubField");
    public static final Key<OdooModelClass> MODEL_CLASS = new Key<>("modelClass");
    public static final Key<Computable<OdooModelClass>> MODEL_CLASS_RESOLVER = new Key<>("modelClassResolver");
    public static final Key<Boolean> IS_SORT_ORDER = new Key<>("isSortOrder");

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element,
                                                 @NotNull ProcessingContext context) {
        OdooModelClass modelClass = context.get(MODEL_CLASS);
        Computable<OdooModelClass> modelClassResolver = context.get(MODEL_CLASS_RESOLVER);
        Boolean enableSubField = context.get(ENABLE_SUB_FIELD);
        Boolean isSortOrder = context.get(IS_SORT_ORDER);
        if (isSortOrder != null && isSortOrder) {
            Collection<PsiReference> references = new LinkedList<>();
            Collection<TextRange> ranges = getReferenceTextRangesInSortOrder(element);
            for (TextRange range : ranges) {
                references.add(new OdooFieldReference(element, range, modelClass, modelClassResolver, null));
            }
            return references.toArray(OdooFieldReference.EMPTY_ARRAY);
        }
        if (enableSubField != null && enableSubField) {
            OdooFieldPathReferences fieldPathReferences = OdooFieldPathReferences.create(element, modelClass, modelClassResolver);
            return fieldPathReferences.getReferences();
        }
        return new PsiReference[]{new OdooFieldReference(element, null, modelClass, modelClassResolver, null)};
    }

    public static Collection<TextRange> getReferenceTextRangesInSortOrder(@NotNull PsiElement element) {
        Collection<TextRange> ranges = new LinkedList<>();
        TextRange range = ElementManipulators.getValueTextRange(element);
        String rangeValue = range.substring(element.getText());
        int idx = range.getStartOffset();
        Pattern pattern = Pattern.compile("(\\w+)\\s*(\\w*)");
        Matcher matcher = pattern.matcher(rangeValue);
        while (matcher.find()) {
            if (matcher.groupCount() > 0) {
                ranges.add(TextRange.create(idx + matcher.start(1), idx + matcher.end(1)));
            }
        }
        return ranges;
    }
}
