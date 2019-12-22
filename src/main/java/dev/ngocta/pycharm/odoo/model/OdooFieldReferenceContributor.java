package dev.ngocta.pycharm.odoo.model;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooFieldReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> MAPPED_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(
                    psiElement(PyArgumentList.class).withParent(
                            psiElement(PyCallExpression.class).with(new PatternCondition<PyCallExpression>("mapped") {
                                @Override
                                public boolean accepts(@NotNull PyCallExpression callExpression, ProcessingContext context) {
                                    PyExpression callee = callExpression.getCallee();
                                    if (callee instanceof PyReferenceExpression) {
                                        PyReferenceExpression referenceExpression = (PyReferenceExpression) callee;
                                        if (OdooNames.MAPPED.equals(referenceExpression.getName())) {
                                            PyExpression qualifier = referenceExpression.getQualifier();
                                            if (qualifier != null) {
                                                TypeEvalContext typeEvalContext = TypeEvalContext.userInitiated(callExpression.getProject(), callExpression.getContainingFile());
                                                PyType qualifierType = typeEvalContext.getType(qualifier);
                                                if (qualifierType instanceof OdooModelClassType) {
                                                    context.put(OdooFieldReferenceProvider.MODEL_CLASS, ((OdooModelClassType) qualifierType).getPyClass());
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                    return false;
                                }
                            })));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> DEPENDS_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(
                    psiElement(PyArgumentList.class).withParent(
                            psiElement(PyDecorator.class).with(new PatternCondition<PyDecorator>("depends") {
                                @Override
                                public boolean accepts(@NotNull PyDecorator pyDecorator, ProcessingContext context) {
                                    QualifiedName qualifiedName = pyDecorator.getQualifiedName();
                                    if (qualifiedName != null && OdooNames.DECORATOR_DEPENDS.equals(qualifiedName.toString())) {
                                        OdooModelClass cls = OdooModelUtils.getContainingOdooModelClass(pyDecorator);
                                        if (cls != null) {
                                            context.put(OdooFieldReferenceProvider.MODEL_CLASS, cls);
                                            return true;
                                        }
                                    }
                                    return false;
                                }
                            })));

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(MAPPED_PATTERN, new OdooFieldReferenceProvider());
        registrar.registerReferenceProvider(DEPENDS_PATTERN, new OdooFieldReferenceProvider());
    }
}
