package dev.ngocta.pycharm.odoo.model;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooPyUtils;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooModelReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> COMODEL_NAME_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(0, OdooNames.FIELD_ATTR_COMODEL_NAME,
                    OdooNames.FIELD_TYPE_ONE2MANY,
                    OdooNames.FIELD_TYPE_MANY2ONE,
                    OdooNames.FIELD_TYPE_MANY2MANY);

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> INHERIT_PATTERN =
            psiElement(PyStringLiteralExpression.class).afterSiblingSkipping(
                    psiElement().withElementType(PyTokenTypes.EQ),
                    psiElement(PyTargetExpression.class).withName(OdooNames.MODEL_INHERIT));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> INHERIT_LIST_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(
                    psiElement(PyListLiteralExpression.class).afterSiblingSkipping(
                            psiElement().withElementType(PyTokenTypes.EQ),
                            psiElement(PyTargetExpression.class).withName(OdooNames.MODEL_INHERIT)));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> ENV_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(PySubscriptionExpression.class).afterSiblingSkipping(
                    psiElement().withElementType(PyTokenTypes.LBRACE),
                    psiElement(PyExpression.class).with(new PatternCondition<PyExpression>("env") {
                        @Override
                        public boolean accepts(@NotNull PyExpression expression, ProcessingContext context) {
                            TypeEvalContext typeEvalContext = TypeEvalContext.userInitiated(expression.getProject(), expression.getContainingFile());
                            return OdooPyUtils.isEnvironmentReferenceExpression(expression, typeEvalContext);
                        }
                    }));

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(INHERIT_PATTERN, new OdooModelReferenceProvider());
        registrar.registerReferenceProvider(INHERIT_LIST_PATTERN, new OdooModelReferenceProvider());
        registrar.registerReferenceProvider(COMODEL_NAME_PATTERN, new OdooModelReferenceProvider());
        registrar.registerReferenceProvider(ENV_PATTERN, new OdooModelReferenceProvider());
    }
}
