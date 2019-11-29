package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelFunctionFilteredTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression, @NotNull TypeEvalContext context) {
        if (!referenceExpression.isQualified()) {
            PsiPolyVariantReference variantReference = referenceExpression.getReference();
            PsiElement target = variantReference.resolve();
            if (target instanceof PyNamedParameter) {
                PsiElement parent = target.getParent();
                if (parent instanceof PyParameterList) {
                    parent = parent.getParent();
                    if (parent instanceof PyLambdaExpression) {
                        parent = PsiTreeUtil.getParentOfType(parent, PyCallExpression.class);
                        if (parent != null) {
                            PyExpression callee = ((PyCallExpression) parent).getCallee();
                            if (callee instanceof PyReferenceExpression) {
                                if (OdooNames.FILTERED.equals(callee.getName())) {
                                    PyExpression qualifier = ((PyReferenceExpression) callee).getQualifier();
                                    if (qualifier != null) {
                                        PyType qualifierType = context.getType(qualifier);
                                        if (qualifierType instanceof OdooModelClassType) {
                                            return ((OdooModelClassType) qualifierType).getOneRecordVariant();
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
