package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.python.OdooPyUtils;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooModelReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> NAME_PATTERN =
            psiElement(PyStringLiteralExpression.class).afterSiblingSkipping(
                    psiElement().withElementType(PyTokenTypes.EQ),
                    psiElement(PyTargetExpression.class).withName(OdooNames.MODEL_NAME));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> INHERIT_PATTERN =
            psiElement(PyStringLiteralExpression.class).afterSiblingSkipping(
                    psiElement().withElementType(PyTokenTypes.EQ),
                    psiElement(PyTargetExpression.class).withName(OdooNames.MODEL_INHERIT));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> INHERIT_LIST_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(
                    psiElement(PyListLiteralExpression.class).afterSiblingSkipping(
                            psiElement().withElementType(PyTokenTypes.EQ),
                            psiElement(PyTargetExpression.class).withName(OdooNames.MODEL_INHERIT)));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> INHERITS_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("inherits") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
                    PsiElement parent = pyStringLiteralExpression.getParent();
                    if (parent instanceof PyKeyValueExpression) {
                        if (pyStringLiteralExpression.equals(((PyKeyValueExpression) parent).getKey())) {
                            parent = parent.getParent();
                        } else {
                            return false;
                        }
                    }
                    return OdooModelUtils.isInheritsAssignedValue(parent);
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> COMODEL_NAME_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(0, OdooNames.FIELD_ATTR_COMODEL_NAME, OdooNames.RELATIONAL_FIELD_TYPES);

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> ENV_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(PySubscriptionExpression.class).afterSiblingSkipping(
                    psiElement().withElementType(PyTokenTypes.LBRACE),
                    psiElement(PyExpression.class).with(new PatternCondition<PyExpression>("env") {
                        @Override
                        public boolean accepts(@NotNull PyExpression expression,
                                               ProcessingContext context) {
                            TypeEvalContext typeEvalContext = TypeEvalContext.codeAnalysis(expression.getProject(), expression.getContainingFile());
                            return OdooPyUtils.isEnvironmentTypeExpression(expression, typeEvalContext);
                        }
                    }));

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        OdooModelReferenceProvider provider = new OdooModelReferenceProvider();
        registrar.registerReferenceProvider(NAME_PATTERN, provider);
        registrar.registerReferenceProvider(INHERIT_PATTERN, provider);
        registrar.registerReferenceProvider(INHERIT_LIST_PATTERN, provider);
        registrar.registerReferenceProvider(INHERITS_PATTERN, provider);
        registrar.registerReferenceProvider(COMODEL_NAME_PATTERN, provider);
        registrar.registerReferenceProvider(ENV_PATTERN, provider);
    }
}
