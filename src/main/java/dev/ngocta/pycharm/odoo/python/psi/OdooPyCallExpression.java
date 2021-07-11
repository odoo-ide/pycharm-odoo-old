package dev.ngocta.pycharm.odoo.python.psi;

import com.google.common.collect.ImmutableSet;
import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyCallExpressionImpl;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyNoneType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassSuperType;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class OdooPyCallExpression extends PyCallExpressionImpl {
    private static final Set<String> COMMON_ORM_METHODS_RETURN_SAME_MODEL = ImmutableSet.of(
            OdooNames.CREATE,
            OdooNames.COPY,
            OdooNames.SEARCH,
            OdooNames.FILTERED,
            OdooNames.SORTED,
            OdooNames.BROWSE,
            OdooNames.WITH_CONTEXT,
            OdooNames.WITH_ENV,
            OdooNames.SUDO,
            OdooNames.EXISTS
    );

    public OdooPyCallExpression(ASTNode astNode) {
        super(astNode);
    }

    @Nullable
    @Override
    public PyType getType(@NotNull TypeEvalContext context,
                          @NotNull TypeEvalContext.Key key) {
        PyType type = null;
        PyExpression callee = getCallee();
        boolean isSuperCall = false;
        if (callee instanceof PyReferenceExpression) {
            String calleeName = callee.getName();
            if (PyNames.SUPER.equals(callee.getText())) {
                isSuperCall = true;
                type = getOdooModelClassSuperType(context);
            } else if (COMMON_ORM_METHODS_RETURN_SAME_MODEL.contains(calleeName)) {
                type = getReceiverType(context);
            }
        }
        OdooModelClassType modelClassType = OdooModelUtils.extractOdooModelClassType(type);
        if (modelClassType != null) {
            if (modelClassType instanceof OdooModelClassSuperType && !isSuperCall) {
                return ((OdooModelClassSuperType) modelClassType).getOrigin();
            }
            return modelClassType;
        }
        type = super.getType(context, key);
        if (type instanceof OdooModelClassType) {
            PyType receiverType = getReceiverType(context);
            if (receiverType instanceof OdooModelClassType) {
                PyClass receiverClass = ((OdooModelClassType) receiverType).getPyClass();
                PyClass callTypeClass = ((OdooModelClassType) type).getPyClass();
                if (receiverClass.isSubclass(callTypeClass, context)) {
                    return receiverType;
                }
            }
        } else if (type instanceof PyNoneType) {
            return null;
        }
        return type;
    }

    @Nullable
    private PyType getReceiverType(@NotNull TypeEvalContext context) {
        PyExpression receiver = getReceiver(null);
        if (receiver != null) {
            return context.getType(receiver);
        }
        return null;
    }

    private PyType getOdooModelClassSuperType(TypeEvalContext context) {
        OdooModelClassType modelClassType = null;
        PyClass anchor = null;
        PyExpression[] args = getArguments();
        if (args.length == 2) {
            PyType superType = context.getType(args[0]);
            if (superType instanceof PyClassType) {
                anchor = ((PyClassType) superType).getPyClass();
            }
            modelClassType = ObjectUtils.tryCast(context.getType(args[1]), OdooModelClassType.class);
        } else if (args.length == 0) {
            PyFunction function = PsiTreeUtil.getParentOfType(this, PyFunction.class);
            PyClass cls = PsiTreeUtil.getParentOfType(this, PyClass.class);
            if (function != null && cls != null) {
                PyParameter[] parameters = function.getParameterList().getParameters();
                if (parameters.length > 0) {
                    if (parameters[0].isSelf() && parameters[0] instanceof PyNamedParameter) {
                        PyType selfType = context.getType((PyNamedParameter) parameters[0]);
                        if (selfType instanceof OdooModelClassType) {
                            modelClassType = (OdooModelClassType) selfType;
                            anchor = cls;
                        }
                    }
                }
            }
        }
        if (modelClassType != null && anchor != null) {
            return new OdooModelClassSuperType(modelClassType, anchor);
        }
        return null;
    }
}
