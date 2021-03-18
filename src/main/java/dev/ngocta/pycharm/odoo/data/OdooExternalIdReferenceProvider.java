package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdooExternalIdReferenceProvider extends PsiReferenceProvider {
    public static final Key<OdooRecordFilter> FILTER = new Key<>("filter");
    public static final Key<Boolean> ALLOW_RELATIVE = new Key<>("allowRelative");
    public static final Key<Boolean> COMMA_SEPARATED = new Key<>("commaSeparated");

    @Override
    public PsiReference @NotNull [] getReferencesByElement(@NotNull PsiElement element,
                                                           @NotNull ProcessingContext context) {
        OdooRecordFilter filter = context.get(FILTER);
        Boolean allowRelative = context.get(ALLOW_RELATIVE);
        Boolean commaSeparated = context.get(COMMA_SEPARATED);
        if (Boolean.TRUE.equals(commaSeparated)) {
            return getCommaSeparatedReferences(element, filter, Boolean.TRUE.equals(allowRelative));
        }
        return new PsiReference[]{new OdooExternalIdReference(element, null, filter, Boolean.TRUE.equals(allowRelative))};
    }

    @NotNull
    public static PsiReference[] getCommaSeparatedReferences(@NotNull PsiElement element,
                                                             @Nullable OdooRecordFilter filter,
                                                             boolean allowRelative) {
        List<PsiReference> result = new LinkedList<>();
        TextRange textRange = ElementManipulators.getValueTextRange(element);
        String text = textRange.substring(element.getText());
        Matcher matcher = Pattern.compile("(\\w+\\.)?\\w+").matcher(text);
        while (matcher.find()) {
            TextRange subRange = textRange.cutOut(new TextRange(matcher.start(), matcher.end()));
            result.add(new OdooExternalIdReference(element, subRange, filter, allowRelative));
        }
        return result.toArray(PsiReference.EMPTY_ARRAY);
    }
}
