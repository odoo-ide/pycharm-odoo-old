package dev.ngocta.pycharm.odoo.model;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
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
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("mapped") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
                    PsiElement parent = pyStringLiteralExpression.getParent();
                    if (parent instanceof PyArgumentList) {
                        parent = parent.getParent();
                        if (parent instanceof PyCallExpression) {
                            PyCallExpression callExpression = (PyCallExpression) parent;
                            PyExpression callee = callExpression.getCallee();
                            if (callee instanceof PyReferenceExpression) {
                                PyReferenceExpression referenceExpression = (PyReferenceExpression) callee;
                                if (OdooNames.MAPPED.equals(referenceExpression.getName())) {
                                    PyExpression qualifier = referenceExpression.getQualifier();
                                    if (qualifier != null) {
                                        context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD, true);
                                        context.put(OdooFieldReferenceProvider.MODEL_CLASS_RESOLVER, () -> {
                                            TypeEvalContext typeEvalContext = TypeEvalContext.userInitiated(
                                                    callExpression.getProject(), callExpression.getContainingFile());
                                            PyType qualifierType = typeEvalContext.getType(qualifier);
                                            OdooModelClassType modelClassType = OdooModelUtils.extractOdooModelClassType(qualifierType);
                                            if (modelClassType != null) {
                                                return modelClassType.getPyClass();
                                            }
                                            return null;
                                        });
                                        return true;
                                    }
                                }
                            }
                            return false;
                        }
                    }
                    return false;
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> DECORATOR_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("decorator") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
                    PsiElement parent = pyStringLiteralExpression.getParent();
                    if (parent instanceof PyArgumentList) {
                        parent = parent.getParent();
                        if (parent instanceof PyDecorator) {
                            PyDecorator decorator = (PyDecorator) parent;
                            QualifiedName qualifiedName = decorator.getQualifiedName();
                            if (qualifiedName != null) {
                                String decoratorName = qualifiedName.toString();
                                if (OdooNames.DECORATOR_DEPENDS.equals(decoratorName)
                                        || OdooNames.DECORATOR_CONSTRAINS.equals(decoratorName)
                                        || OdooNames.DECORATOR_ONCHANGE.equals(decoratorName)) {
                                    context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD,
                                            OdooNames.DECORATOR_DEPENDS.equals(decoratorName));
                                    context.put(OdooFieldReferenceProvider.MODEL_CLASS_RESOLVER, () -> {
                                        return OdooModelUtils.getContainingOdooModelClass(decorator);
                                    });
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> ONE2MANY_INVERSE_NAME_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(1, OdooNames.FIELD_ATTR_INVERSE_NAME, OdooNames.FIELD_TYPE_ONE2MANY)
                    .with(new PatternCondition<PyStringLiteralExpression>("inverseName") {
                        @Override
                        public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                               ProcessingContext context) {
                            context.put(OdooFieldReferenceProvider.MODEL_CLASS_RESOLVER, () -> {
                                PyCallExpression callExpression = PsiTreeUtil.getParentOfType(pyStringLiteralExpression, PyCallExpression.class);
                                if (callExpression != null) {
                                    PyStringLiteralExpression comodelExpression = callExpression.getArgument(
                                            0, OdooNames.FIELD_ATTR_COMODEL_NAME, PyStringLiteralExpression.class);
                                    if (comodelExpression != null) {
                                        return OdooModelClass.getInstance(comodelExpression.getStringValue(), callExpression.getProject());
                                    }
                                }
                                return null;
                            });
                            return true;
                        }
                    });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> RELATED_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(-1, OdooNames.FIELD_ATTR_RELATED)
                    .with(new PatternCondition<PyStringLiteralExpression>("related") {
                        @Override
                        public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                               ProcessingContext context) {
                            context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD, true);
                            context.put(OdooFieldReferenceProvider.MODEL_CLASS_RESOLVER, () -> {
                                return OdooModelUtils.getContainingOdooModelClass(pyStringLiteralExpression);
                            });
                            return true;
                        }
                    });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> CURRENCY_FIELD_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(-1, OdooNames.FIELD_ATTR_CURRENCY_FIELD, OdooNames.FIELD_TYPE_MONETARY)
                    .with(new PatternCondition<PyStringLiteralExpression>("currencyField") {
                        @Override
                        public boolean accepts(@NotNull PyStringLiteralExpression expression,
                                               ProcessingContext context) {
                            context.put(OdooFieldReferenceProvider.MODEL_CLASS_RESOLVER, () -> {
                                return OdooModelUtils.getContainingOdooModelClass(expression);
                            });
                            return true;
                        }
                    });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> SEARCH_DOMAIN_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("searchDomain") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyReferenceExpression,
                                       ProcessingContext context) {
                    PyListLiteralExpression domainExpression = OdooModelUtils.getSearchDomainExpression(pyReferenceExpression);
                    if (domainExpression == null) {
                        return false;
                    }
                    context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD, true);
                    context.put(OdooFieldReferenceProvider.MODEL_CLASS_RESOLVER, () -> {
                        return OdooModelUtils.resolveSearchDomainContext(domainExpression, true);
                    });
                    return true;
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> RECORD_VALUE_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("createValue") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
                    PsiElement valueExpression = OdooModelUtils.getRecordValueExpression(pyStringLiteralExpression);
                    if (valueExpression == null) {
                        return false;
                    }
                    context.put(OdooFieldReferenceProvider.MODEL_CLASS_RESOLVER, () -> {
                        return OdooModelUtils.resolveRecordValueContext(valueExpression);
                    });
                    return true;
                }
            });

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        OdooFieldReferenceProvider provider = new OdooFieldReferenceProvider();
        registrar.registerReferenceProvider(MAPPED_PATTERN, provider);
        registrar.registerReferenceProvider(DECORATOR_PATTERN, provider);
        registrar.registerReferenceProvider(ONE2MANY_INVERSE_NAME_PATTERN, provider);
        registrar.registerReferenceProvider(RELATED_PATTERN, provider);
        registrar.registerReferenceProvider(CURRENCY_FIELD_PATTERN, provider);
        registrar.registerReferenceProvider(SEARCH_DOMAIN_PATTERN, provider);
        registrar.registerReferenceProvider(RECORD_VALUE_PATTERN, provider);
    }
}
