package dev.ngocta.pycharm.odoo.module;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyKeyValueExpression;
import com.jetbrains.python.psi.PyListLiteralExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import dev.ngocta.pycharm.odoo.OdooFilePathReferenceProvider;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooManifestReferenceContributor extends PsiReferenceContributor {
    static class ListItemPatternCondition extends PatternCondition<PsiElement> {
        private final List<String> myKeys;

        public ListItemPatternCondition(@NotNull String... keys) {
            super("manifest");
            myKeys = Arrays.asList(keys);
        }

        @Override
        public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
            PsiElement parent = element.getParent();
            if (parent instanceof PyListLiteralExpression) {
                parent = parent.getParent();
                if (parent instanceof PyKeyValueExpression) {
                    PsiFile file = parent.getContainingFile();
                    if (file != null && OdooNames.MANIFEST_FILE_NAME.equals(file.getName())) {
                        PyExpression keyExpression = ((PyKeyValueExpression) parent).getKey();
                        return keyExpression instanceof PyStringLiteralExpression
                                && myKeys.contains(((PyStringLiteralExpression) keyExpression).getStringValue());
                    }
                }
            }
            return false;
        }
    }

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> DEPEND_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new ListItemPatternCondition(OdooNames.MANIFEST_DEPENDS));
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> FILE_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new ListItemPatternCondition(
                    OdooNames.MANIFEST_DATA, OdooNames.MANIFEST_DEMO, OdooNames.MANIFEST_QWEB));

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(DEPEND_PATTERN, new OdooModuleReferenceProvider());
        registrar.registerReferenceProvider(FILE_PATTERN, new OdooFilePathReferenceProvider());
    }
}
