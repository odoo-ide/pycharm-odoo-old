package dev.ngocta.pycharm.odoo.model;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.PsiTreeUtil;
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
                                public boolean accepts(@NotNull PyCallExpression callExpression,
                                                       ProcessingContext context) {
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
                                                    context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD, true);
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                    return false;
                                }
                            })));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> DECORATORS_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(
                    psiElement(PyArgumentList.class).withParent(
                            psiElement(PyDecorator.class).with(new PatternCondition<PyDecorator>("decorators") {
                                @Override
                                public boolean accepts(@NotNull PyDecorator pyDecorator,
                                                       ProcessingContext context) {
                                    QualifiedName qualifiedName = pyDecorator.getQualifiedName();
                                    if (qualifiedName != null) {
                                        String s = qualifiedName.toString();
                                        if (OdooNames.DECORATOR_DEPENDS.equals(s)
                                                || OdooNames.DECORATOR_CONSTRAINS.equals(s)
                                                || OdooNames.DECORATOR_ONCHANGE.equals(s)) {
                                            OdooModelClass cls = OdooModelUtils.getContainingOdooModelClass(pyDecorator);
                                            if (cls != null) {
                                                boolean enableSubField = OdooNames.DECORATOR_DEPENDS.equals(s);
                                                context.put(OdooFieldReferenceProvider.MODEL_CLASS, cls);
                                                context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD, enableSubField);
                                                return true;
                                            }
                                        }
                                    }
                                    return false;
                                }
                            })));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> ONE2MANY_INVERSE_NAME_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(1, OdooNames.FIELD_ATTR_INVERSE_NAME, OdooNames.FIELD_TYPE_ONE2MANY)
                    .with(new PatternCondition<PyStringLiteralExpression>("inverseName") {
                        @Override
                        public boolean accepts(@NotNull PyStringLiteralExpression inverseNameExpression,
                                               ProcessingContext context) {
                            PyCallExpression callExpression = PsiTreeUtil.getParentOfType(inverseNameExpression, PyCallExpression.class);
                            if (callExpression != null) {
                                PyStringLiteralExpression comodelNameExpression = callExpression.getArgument(0, OdooNames.FIELD_ATTR_COMODEL_NAME, PyStringLiteralExpression.class);
                                if (comodelNameExpression != null) {
                                    OdooModelClass cls = OdooModelClass.getInstance(comodelNameExpression.getStringValue(), inverseNameExpression.getProject());
                                    context.put(OdooFieldReferenceProvider.MODEL_CLASS, cls);
                                    return true;
                                }
                            }
                            return false;
                        }
                    });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> RELATED_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(
                    psiElement(PyKeywordArgument.class).with(new PatternCondition<PyKeywordArgument>("related") {
                        @Override
                        public boolean accepts(@NotNull PyKeywordArgument pyKeywordArgument,
                                               ProcessingContext context) {
                            if (OdooNames.FIELD_ATTR_RELATED.equals(pyKeywordArgument.getKeyword())) {
                                PyCallExpression callExpression = PsiTreeUtil.getParentOfType(pyKeywordArgument, PyCallExpression.class);
                                if (callExpression != null && OdooModelUtils.isFieldDeclarationExpression(callExpression)) {
                                    OdooModelClass cls = OdooModelUtils.getContainingOdooModelClass(pyKeywordArgument);
                                    if (cls != null) {
                                        context.put(OdooFieldReferenceProvider.MODEL_CLASS, cls);
                                        context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD, true);
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    }));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> CURRENCY_FIELD_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(-1, OdooNames.FIELD_ATTR_CURRENCY_FIELD, OdooNames.FIELD_TYPE_MONETARY)
                    .with(new PatternCondition<PyStringLiteralExpression>("currencyField") {
                        @Override
                        public boolean accepts(@NotNull PyStringLiteralExpression expression,
                                               ProcessingContext context) {
                            OdooModelClass cls = OdooModelUtils.getContainingOdooModelClass(expression);
                            if (cls != null) {
                                context.put(OdooFieldReferenceProvider.MODEL_CLASS, cls);
                                return true;
                            }
                            return false;
                        }
                    });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> SEARCH_DOMAIN_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("searchDomain") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyReferenceExpression,
                                       ProcessingContext context) {
                    OdooModelClass cls = OdooModelUtils.getSearchDomainModelContext(pyReferenceExpression);
                    if (cls != null) {
                        context.put(OdooFieldReferenceProvider.MODEL_CLASS, cls);
                        context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD, true);
                        return true;
                    }
                    return false;
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> RECORD_VALUE_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("createValue") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
                    OdooModelClass cls = OdooModelUtils.getRecordValueModelContext(pyStringLiteralExpression);
                    if (cls != null) {
                        context.put(OdooFieldReferenceProvider.MODEL_CLASS, cls);
                        return true;
                    }
                    return false;
                }
            });

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        OdooFieldReferenceProvider provider = new OdooFieldReferenceProvider();
        registrar.registerReferenceProvider(MAPPED_PATTERN, provider);
        registrar.registerReferenceProvider(DECORATORS_PATTERN, provider);
        registrar.registerReferenceProvider(ONE2MANY_INVERSE_NAME_PATTERN, provider);
        registrar.registerReferenceProvider(RELATED_PATTERN, provider);
        registrar.registerReferenceProvider(CURRENCY_FIELD_PATTERN, provider);
        registrar.registerReferenceProvider(SEARCH_DOMAIN_PATTERN, provider);
        registrar.registerReferenceProvider(RECORD_VALUE_PATTERN, provider);
    }
}
