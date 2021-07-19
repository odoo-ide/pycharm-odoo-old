package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdooFieldReferenceProvider extends PsiReferenceProvider {
    public static final Key<Boolean> ENABLE_SUB_FIELD = new Key<>("enableSubField");
    public static final Key<OdooModelClass> MODEL_CLASS = new Key<>("modelClass");
    public static final Key<Computable<OdooModelClass>> MODEL_CLASS_RESOLVER = new Key<>("modelClassResolver");
    public static final Key<Boolean> IS_SORT_ORDER = new Key<>("isSortOrder");
    public static final Key<Boolean> IS_READ_GROUP_FIELDS = new Key<>("isReadGroupFields");

    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                           @NotNull ProcessingContext context) {
        OdooModelClass modelClass = context.get(MODEL_CLASS);
        Computable<OdooModelClass> modelClassResolver = context.get(MODEL_CLASS_RESOLVER);
        List<PsiReference> references = new LinkedList<>();
        if (OdooUtils.isTrue(context.get(IS_SORT_ORDER))) {
            Collection<TextRange> ranges = getReferenceTextRangesInSortOrder(element);
            for (TextRange r : ranges) {
                references.add(new OdooFieldReference(element, r, modelClass, modelClassResolver, null));
            }
        } else if (OdooUtils.isTrue(context.get(IS_READ_GROUP_FIELDS))) {
            TextRange range = getReferenceTextRangeInReadGroupFields(element);
            references.add(new OdooFieldReference(element, range, modelClass, modelClassResolver, null));
        } else if (OdooUtils.isTrue(context.get(ENABLE_SUB_FIELD))) {
            OdooFieldPathReferences fieldPathReferences = OdooFieldPathReferences.create(element, modelClass, modelClassResolver);
            return fieldPathReferences.getReferences();
        } else {
            references.add(new OdooFieldReference(element, null, modelClass, modelClassResolver, null));
        }
        return references.toArray(OdooFieldReference.EMPTY_ARRAY);
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

    @Nullable
    public static TextRange getReferenceTextRangeInReadGroupFields(@NotNull PsiElement element) {
        TextRange range = ElementManipulators.getValueTextRange(element);
        String rangeValue = range.substring(element.getText());
        Matcher matcher = Pattern.compile("(\\w*):\\w+(\\((\\w*)\\))?").matcher(rangeValue);
        if (matcher.find()) {
            if (matcher.group(3) != null) {
                return new TextRange(range.getStartOffset() + matcher.start(3), range.getStartOffset() + matcher.end(3));
            } else {
                return new TextRange(range.getStartOffset(), range.getStartOffset() + matcher.end(1));
            }
        }
        return null;
    }
}
