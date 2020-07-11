package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.QualifiedName;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.codeInsight.controlflow.ScopeOwner;
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooFieldReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> INHERITS_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("inherits") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
                    PsiElement parent = pyStringLiteralExpression.getParent();
                    if (parent instanceof PyKeyValueExpression) {
                        if (pyStringLiteralExpression.equals(((PyKeyValueExpression) parent).getValue())) {
                            parent = parent.getParent();
                            if (OdooModelUtils.isInheritsAssignedValue(parent)) {
                                OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(pyStringLiteralExpression);
                                context.put(OdooFieldReferenceProvider.MODEL_CLASS, modelClass);
                                return true;
                            }
                            return false;
                        }
                    }
                    return false;
                }
            });

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
                                        TypeEvalContext typeEvalContext = TypeEvalContext.userInitiated(
                                                callExpression.getProject(), callExpression.getContainingFile());
                                        PyType qualifierType = typeEvalContext.getType(qualifier);
                                        OdooModelClassType modelClassType = OdooModelUtils.extractOdooModelClassType(qualifierType);
                                        OdooModelClass modelClass = modelClassType != null ? modelClassType.getPyClass() : null;
                                        context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD, true);
                                        context.put(OdooFieldReferenceProvider.MODEL_CLASS, modelClass);
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
                                if (OdooNames.API_DEPENDS.equals(decoratorName)
                                        || OdooNames.API_CONSTRAINS.equals(decoratorName)
                                        || OdooNames.API_ONCHANGE.equals(decoratorName)) {
                                    context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD,
                                            OdooNames.API_DEPENDS.equals(decoratorName));
                                    OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(decorator);
                                    context.put(OdooFieldReferenceProvider.MODEL_CLASS, modelClass);
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
                            PyCallExpression callExpression = PsiTreeUtil.getParentOfType(pyStringLiteralExpression, PyCallExpression.class);
                            if (callExpression != null) {
                                PyStringLiteralExpression comodelExpression = callExpression.getArgument(
                                        0, OdooNames.FIELD_ATTR_COMODEL_NAME, PyStringLiteralExpression.class);
                                if (comodelExpression != null) {
                                    OdooModelClass modelClass = OdooModelClass.getInstance(comodelExpression.getStringValue(), callExpression.getProject());
                                    context.put(OdooFieldReferenceProvider.MODEL_CLASS, modelClass);
                                }
                            }
                            return true;
                        }
                    });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> RELATED_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(-1, OdooNames.FIELD_ATTR_RELATED)
                    .with(new PatternCondition<PyStringLiteralExpression>("related") {
                        @Override
                        public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                               ProcessingContext context) {
                            OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(pyStringLiteralExpression);
                            context.put(OdooFieldReferenceProvider.ENABLE_SUB_FIELD, true);
                            context.put(OdooFieldReferenceProvider.MODEL_CLASS, modelClass);
                            return true;
                        }
                    });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> CURRENCY_FIELD_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(-1, OdooNames.FIELD_ATTR_CURRENCY_FIELD, OdooNames.FIELD_TYPE_MONETARY)
                    .with(new PatternCondition<PyStringLiteralExpression>("currencyField") {
                        @Override
                        public boolean accepts(@NotNull PyStringLiteralExpression expression,
                                               ProcessingContext context) {
                            OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(expression);
                            context.put(OdooFieldReferenceProvider.MODEL_CLASS, modelClass);
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
                    OdooModelClass modelClass = OdooModelUtils.resolveSearchDomainContext(domainExpression, true);
                    if (modelClass != null || maybeContainFieldReferences(domainExpression)) {
                        context.put(OdooFieldReferenceProvider.MODEL_CLASS, modelClass);
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
                    PsiElement valueExpression = OdooModelUtils.getRecordValueExpression(pyStringLiteralExpression);
                    if (valueExpression == null) {
                        return false;
                    }
                    OdooModelClass modelClass = OdooModelUtils.resolveRecordValueContext(valueExpression);
                    if (modelClass != null || maybeContainFieldReferences(valueExpression)) {
                        context.put(OdooFieldReferenceProvider.MODEL_CLASS, modelClass);
                        return true;
                    }
                    return false;
                }
            });

    private static boolean maybeContainFieldReferences(PsiElement element) {
        ScopeOwner scopeOwner = ScopeUtil.getScopeOwner(element);
        if (scopeOwner instanceof PyFunction) {
            return true;
        }
        return false;
    }

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        OdooFieldReferenceProvider provider = new OdooFieldReferenceProvider();
        registrar.registerReferenceProvider(INHERITS_PATTERN, provider);
        registrar.registerReferenceProvider(MAPPED_PATTERN, provider);
        registrar.registerReferenceProvider(DECORATOR_PATTERN, provider);
        registrar.registerReferenceProvider(ONE2MANY_INVERSE_NAME_PATTERN, provider);
        registrar.registerReferenceProvider(RELATED_PATTERN, provider);
        registrar.registerReferenceProvider(CURRENCY_FIELD_PATTERN, provider);
        registrar.registerReferenceProvider(SEARCH_DOMAIN_PATTERN, provider);
        registrar.registerReferenceProvider(RECORD_VALUE_PATTERN, provider);
    }
}
