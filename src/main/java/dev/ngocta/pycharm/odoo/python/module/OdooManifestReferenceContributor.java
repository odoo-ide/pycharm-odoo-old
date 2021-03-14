package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyKeyValueExpression;
import com.jetbrains.python.psi.PyListLiteralExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import dev.ngocta.pycharm.odoo.OdooFilePathReferenceProvider;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooManifestReferenceContributor extends PsiReferenceContributor {
    public static String[] HOOK_FUNCTION_KEYS = new String[]{"pre_init_hook", "post_init_hook", "uninstall_hook"};

    static class ListItemPatternCondition extends PatternCondition<PsiElement> {
        private final String[] myKeys;

        public ListItemPatternCondition(@NotNull String... keys) {
            super("manifest");
            myKeys = keys;
        }

        @Override
        public boolean accepts(@NotNull PsiElement element,
                               ProcessingContext context) {
            PsiElement parent = element.getParent();
            if (parent instanceof PyListLiteralExpression) {
                parent = parent.getParent();
                if (parent instanceof PyKeyValueExpression) {
                    PsiFile file = parent.getContainingFile();
                    if (file != null && OdooNames.MANIFEST_FILE_NAME.equals(file.getName())) {
                        PyExpression keyExpression = ((PyKeyValueExpression) parent).getKey();
                        return keyExpression instanceof PyStringLiteralExpression
                                && ArrayUtil.contains(((PyStringLiteralExpression) keyExpression).getStringValue(), myKeys);
                    }
                }
            }
            return false;
        }
    }

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> DEPEND_PATTERN =
            psiElement(PyStringLiteralExpression.class)
                    .with(new ListItemPatternCondition(OdooNames.MANIFEST_DEPENDS));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> FILE_PATH_PATTERN =
            psiElement(PyStringLiteralExpression.class)
                    .with(new ListItemPatternCondition(OdooNames.MANIFEST_DATA, OdooNames.MANIFEST_DEMO,
                            OdooNames.MANIFEST_QWEB, OdooNames.MANIFEST_IMAGES));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> AUTO_INSTALL_PATTERN =
            psiElement(PyStringLiteralExpression.class)
                    .with(new ListItemPatternCondition(OdooNames.MANIFEST_AUTO_INSTALL));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> HOOK_FUNCTION_PATTERN =
            psiElement(PyStringLiteralExpression.class)
                    .with(new PatternCondition<PyStringLiteralExpression>("hookFunction") {
                        @Override
                        public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                               ProcessingContext context) {
                            PsiElement parent = pyStringLiteralExpression.getParent();
                            if (parent instanceof PyKeyValueExpression && pyStringLiteralExpression.equals(((PyKeyValueExpression) parent).getValue())) {
                                PsiElement key = ((PyKeyValueExpression) parent).getKey();
                                if (key instanceof PyStringLiteralExpression) {
                                    String keyValue = ((PyStringLiteralExpression) key).getStringValue();
                                    if (ArrayUtil.contains(keyValue, HOOK_FUNCTION_KEYS)) {
                                        PsiFile file = parent.getContainingFile();
                                        return OdooNames.MANIFEST_FILE_NAME.equals(file.getName());
                                    }
                                }
                            }
                            return false;
                        }
                    });

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(DEPEND_PATTERN, new OdooModuleReferenceProvider());
        registrar.registerReferenceProvider(FILE_PATH_PATTERN, new OdooFilePathReferenceProvider());
        registrar.registerReferenceProvider(AUTO_INSTALL_PATTERN, new OdooModuleReferenceProvider());
        registrar.registerReferenceProvider(HOOK_FUNCTION_PATTERN, new OdooModuleHookFunctionReferenceProvider());
    }
}
