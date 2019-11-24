package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;
import com.jetbrains.python.psi.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getParameterType(@NotNull PyNamedParameter param, @NotNull PyFunction function, @NotNull TypeEvalContext context) {
        if (param.isSelf()) {
            PyClass pyClass = PyUtil.getContainingClassOrSelf(param);
            if (pyClass != null) {
                PsiElement parent = param.getParent();
                if (parent instanceof PyParameterList) {
                    PyParameterList parameterList = (PyParameterList) parent;
                    PyFunction func = parameterList.getContainingFunction();
                    if (func != null) {
                        final PyFunction.Modifier modifier = func.getModifier();
                        OdooModelClassType type = OdooModelClassType.create(pyClass, modifier == PyFunction.Modifier.CLASSMETHOD);
                        if (type != null) {
                            return Ref.create(type);
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression, @NotNull TypeEvalContext context) {
        PyPsiFacade psiFacade = PyPsiFacade.getInstance(referenceExpression.getProject());
        String referenceName = referenceExpression.getName();
        PyExpression qualifier = referenceExpression.getQualifier();
        if (qualifier != null) {
            PyType qualifierType = context.getType(qualifier);
            if (OdooNames.ENV.equals(referenceName) && qualifierType instanceof OdooModelClassType) {
                PyClass envClass = psiFacade.createClassByQName(OdooNames.ODOO_API_ENVIRONMENT, referenceExpression);
                if (envClass != null) {
                    return new PyClassTypeImpl(envClass, false);
                }
            } else if (OdooNames.USER.equals(referenceName) && qualifierType instanceof PyClassType) {
                if (OdooNames.ODOO_API_ENVIRONMENT.equals(((PyClassType) qualifierType).getClassQName())) {
                    return findModelClassType(OdooNames.RES_USERS, referenceExpression, OdooRecordSetType.MODEL);
                }
            }
        }

        PsiPolyVariantReference variantReference = referenceExpression.getReference();
        PsiElement psiElement = variantReference.resolve();
        if (psiElement instanceof PyTargetExpression) {
            return getFieldType((PyTargetExpression) psiElement, context, referenceExpression);
        }

        return null;
    }

    @Nullable
    private PyType getFieldType(PyTargetExpression field, TypeEvalContext context, PsiElement anchor) {
        PyExpression pyExpression = field.findAssignedValue();
        if (pyExpression instanceof PyCallExpression) {
            PyCallExpression callExpression = (PyCallExpression) pyExpression;
            PyExpression callee = callExpression.getCallee();
            PyPsiFacade psiFacade = PyPsiFacade.getInstance(anchor.getProject());
            if (callee != null) {
                String calleeName = callee.getName();
                PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(anchor);
                if (calleeName != null) {
                    switch (calleeName) {
                        case OdooNames.MANY2ONE:
                        case OdooNames.ONE2MANY:
                        case OdooNames.MANY2MANY:
                            PyStringLiteralExpression comodelExpression = callExpression.getArgument(
                                    0, OdooNames.COMODEL_NAME, PyStringLiteralExpression.class);
                            if (comodelExpression != null) {
                                String comodel = comodelExpression.getStringValue();
                                return findModelClassType(comodel, anchor, OdooRecordSetType.MULTI);
                            }
                            break;
                        case OdooNames.BOOLEAN:
                            return builtinCache.getBoolType();
                        case OdooNames.INTEGER:
                            return builtinCache.getIntType();
                        case OdooNames.FLOAT:
                        case OdooNames.MONETARY:
                            return builtinCache.getFloatType();
                        case OdooNames.CHAR:
                        case OdooNames.TEXT:
                        case OdooNames.SELECTION:
                            return PyUnionType.union(builtinCache.getStrType(), null);
                        case OdooNames.DATE:
                            PyClass dateClass = psiFacade.createClassByQName("datetime.date", anchor);
                            if (dateClass != null) {
                                return PyUnionType.union(context.getType(dateClass), null);
                            }
                            break;
                        case OdooNames.DATETIME:
                            PyClass datetimeClass = psiFacade.createClassByQName("datetime.datetime", anchor);
                            if (datetimeClass != null) {
                                return PyUnionType.union(context.getType(datetimeClass), null);
                            }
                            break;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private OdooModelClassType findModelClassType(@NotNull String model, @NotNull PsiElement anchor, @Nullable OdooRecordSetType recordSetType) {
        PyClass pyClass = findModelClass(model, anchor);
        if (pyClass != null) {
            return OdooModelClassType.create(pyClass, recordSetType);
        }
        return null;
    }

    @Nullable
    private PyClass findModelClass(@NotNull String model, @NotNull PsiElement anchor) {
        PsiDirectory module = OdooUtils.getOdooModuleDir(anchor);
        if (module != null) {
            return findModelClass(model, module, new LinkedList<>());
        }
        return null;
    }

    @Nullable
    private PyClass findModelClass(@NotNull String model, @NotNull PsiDirectory module, List<PsiDirectory> visitedModules) {
        if (visitedModules.contains(module)) {
            return null;
        }
        visitedModules.add(module);
        List<PyClass> pyClasses = OdooModelIndex.findModelClasses(model, module);
        if (!pyClasses.isEmpty()) {
            return pyClasses.get(0);
        }
        List<PsiDirectory> depends = OdooModuleIndex.getDepends(module);
        for (PsiDirectory depend : depends) {
            PyClass pyClass = findModelClass(model, depend, visitedModules);
            if (pyClass != null) {
                return pyClass;
            }
        }
        return null;
    }

    @Override
    public Ref<PyType> getReferenceType(@NotNull PsiElement referenceTarget, @NotNull TypeEvalContext context, @Nullable PsiElement anchor) {
        if (referenceTarget instanceof PyTargetExpression) {
            PyTargetExpression targetExpression = (PyTargetExpression) referenceTarget;
            PsiElement parent = targetExpression.getParent();
            if (parent instanceof PyForPart) {
                PyForPart forPart = (PyForPart) parent;
                PyExpression source = forPart.getSource();
                if (source instanceof PyReferenceExpression) {
                    PyType referenceType = context.getType(source);
                    if (referenceType instanceof OdooModelClassType) {
                        return Ref.create(((OdooModelClassType) referenceType).getOneRecord());
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public Ref<PyType> getCallType(@NotNull PyFunction function, @NotNull PyCallSiteExpression callSite, @NotNull TypeEvalContext context) {
        String functionName = function.getName();
        if (PyNames.GETITEM.equals(functionName) && callSite instanceof PySubscriptionExpression) {
            PySubscriptionExpression subscription = (PySubscriptionExpression) callSite;
            return getTypeFromEnv(subscription, context);
        }
        if (OdooNames.BROWSE_VARIANTS.contains(functionName) && callSite instanceof PyCallExpression) {
            PyCallExpression browse = (PyCallExpression) callSite;
            PyExpression callee = browse.getCallee();
            if (callee instanceof PyReferenceExpression) {
                PyExpression qualifier = ((PyReferenceExpression) callee).getQualifier();
                if (qualifier != null) {
                    PyType qualifierType = context.getType(qualifier);
                    if (qualifierType instanceof OdooModelClassType) {
                        return Ref.create(qualifierType);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private Ref<PyType> getTypeFromEnv(PySubscriptionExpression envExpression, TypeEvalContext context) {
        PyExpression operand = envExpression.getOperand();
        PyType operandType = context.getType(operand);
        Collection<PyType> candidateTypes;
        if (operandType instanceof PyUnionType) {
            candidateTypes = ((PyUnionType) operandType).getMembers();
        } else {
            candidateTypes = Collections.singleton(operandType);
        }
        for (PyType candidateType : candidateTypes) {
            if (candidateType instanceof PyClassType) {
                PyClassType classType = (PyClassType) candidateType;
                if (OdooNames.ODOO_API_ENVIRONMENT.equals(classType.getClassQName())) {
                    PyExpression index = envExpression.getIndexExpression();
                    if (index instanceof PyLiteralExpression) {
                        String model = ((PyStringLiteralExpressionImpl) index).getStringValue();
                        PsiElement anchor;
                        if (OdooUtils.isOdooModelFile(envExpression.getContainingFile())) {
                            anchor = envExpression;
                        } else {
                            anchor = context.getOrigin();
                        }
                        if (anchor != null) {
                            OdooModelClassType modelClassType = findModelClassType(model, anchor, OdooRecordSetType.MODEL);
                            return Ref.create(modelClassType);
                        }
                    }
                }
            }
        }
        return null;
    }
}
