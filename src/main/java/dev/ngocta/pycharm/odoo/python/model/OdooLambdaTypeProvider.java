package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooLambdaTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression,
                                             @NotNull TypeEvalContext context) {
        if (!referenceExpression.isQualified()) {
            PsiPolyVariantReference variantReference = referenceExpression.getReference();
            PsiElement target = variantReference.resolve();
            if (target instanceof PyNamedParameter) {
                PsiElement parent = target.getParent();
                if (parent instanceof PyParameterList) {
                    parent = parent.getParent();
                    if (parent instanceof PyLambdaExpression) {
                        if (parent.getParent() instanceof PyKeywordArgument) {
                            PyKeywordArgument arg = (PyKeywordArgument) parent.getParent();
                            if (OdooNames.FIELD_ATTR_DEFAULT.equals(arg.getKeyword())) {
                                OdooModelClass cls = OdooModelUtils.getContainingOdooModelClass(referenceExpression);
                                if (cls != null) {
                                    return new OdooModelClassType(cls, OdooRecordSetType.MODEL);
                                }
                            }
                        } else {
                            PyCallExpression callExpression = PsiTreeUtil.getParentOfType(parent, PyCallExpression.class);
                            if (callExpression != null) {
                                PyExpression callee = callExpression.getCallee();
                                if (callee instanceof PyReferenceExpression) {
                                    if (ArrayUtil.contains(callee.getName(), OdooNames.FILTERED, OdooNames.MAPPED, OdooNames.SORTED)) {
                                        PyExpression qualifier = ((PyReferenceExpression) callee).getQualifier();
                                        if (qualifier != null) {
                                            PyType qualifierType = context.getType(qualifier);
                                            if (qualifierType instanceof OdooModelClassType) {
                                                return ((OdooModelClassType) qualifierType).withOneRecord();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }
}
