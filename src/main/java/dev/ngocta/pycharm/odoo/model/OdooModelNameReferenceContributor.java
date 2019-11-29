package dev.ngocta.pycharm.odoo.model;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyArgumentList;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooModelNameReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> COMODEL_NAME_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(
                    psiElement(PyArgumentList.class).afterSibling(
                            psiElement(PyReferenceExpression.class).with(new PatternCondition<PyReferenceExpression>("comodel") {
                                @Override
                                public boolean accepts(@NotNull PyReferenceExpression pyReferenceExpression, ProcessingContext context) {
                                    String name = pyReferenceExpression.getName();
                                    return OdooNames.MANY2ONE.equals(name) || OdooNames.ONE2MANY.equals(name) || OdooNames.MANY2MANY.equals(name);
                                }
                            })));

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(COMODEL_NAME_PATTERN, new OdooModelNameReferenceProvider());
    }
}
