package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyArgumentList;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyKeywordArgument;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooModelFunctionReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> FIELD_ATTR_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression, ProcessingContext context) {
                    PsiElement parent = pyStringLiteralExpression.getParent();
                    if (parent instanceof PyKeywordArgument) {
                        if (ArrayUtil.contains(((PyKeywordArgument) parent).getKeyword(),
                                OdooNames.FIELD_ATTR_COMPUTE,
                                OdooNames.FIELD_ATTR_INVERSE,
                                OdooNames.FIELD_ATTR_SEARCH,
                                OdooNames.FIELD_ATTR_GROUP_EXPAND)) {
                            parent = parent.getParent();
                            if (parent instanceof PyArgumentList) {
                                parent = parent.getParent();
                                return parent instanceof PyCallExpression
                                        && OdooModelUtils.isFieldDeclarationExpression((PyCallExpression) parent);
                            }
                        }
                    }
                    return false;
                }
            });

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(FIELD_ATTR_PATTERN, new OdooModelFunctionReferenceProvider());
    }
}
