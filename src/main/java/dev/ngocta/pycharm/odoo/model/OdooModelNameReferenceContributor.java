package dev.ngocta.pycharm.odoo.model;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooModelNameReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> COMODEL_NAME_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("comodel") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression stringExpression, ProcessingContext context) {
                    PsiElement parent = stringExpression.getParent();
                    if (parent instanceof PyArgumentList || parent instanceof PyKeywordArgument) {
                        PyCallExpression callExpression = PsiTreeUtil.getParentOfType(parent, PyCallExpression.class);
                        if (callExpression != null) {
                            PyExpression callee = callExpression.getCallee();
                            if (callee instanceof PyReferenceExpression) {
                                String calleeName = callee.getName();
                                if (OdooNames.MANY2ONE.equals(calleeName) || OdooNames.ONE2MANY.equals(calleeName) || OdooNames.MANY2MANY.equals(calleeName)) {
                                    PyStringLiteralExpression comodelExpression = callExpression.getArgument(0, OdooNames.FIELD_COMODEL_NAME, PyStringLiteralExpression.class);
                                    return stringExpression.equals(comodelExpression);
                                }
                            }
                        }
                    }
                    return false;
                }
            });

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
                    psiElement(PyReferenceExpression.class).with(new PatternCondition<PyReferenceExpression>("env") {
                        @Override
                        public boolean accepts(@NotNull PyReferenceExpression pyReferenceExpression, ProcessingContext context) {
                            return OdooNames.ENV.equals(pyReferenceExpression.getName());
                        }
                    }));

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(INHERIT_PATTERN, new OdooModelNameInheritReferenceProvider());
        registrar.registerReferenceProvider(INHERIT_LIST_PATTERN, new OdooModelNameInheritReferenceProvider());
        registrar.registerReferenceProvider(COMODEL_NAME_PATTERN, new OdooModelNameReferenceProvider());
        registrar.registerReferenceProvider(ENV_PATTERN, new OdooModelNameReferenceProvider());
    }
}
