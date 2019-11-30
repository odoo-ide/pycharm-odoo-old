package dev.ngocta.pycharm.odoo.model;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
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
                                    PyStringLiteralExpression comodelExpression = callExpression.getArgument(0, OdooNames.COMODEL_NAME, PyStringLiteralExpression.class);
                                    return stringExpression.equals(comodelExpression);
                                }
                            }
                        }
                    }
                    return false;
                }
            });

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(COMODEL_NAME_PATTERN, new OdooModelNameReferenceProvider());
    }
}
