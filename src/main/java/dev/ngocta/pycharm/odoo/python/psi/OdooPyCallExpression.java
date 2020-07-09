package dev.ngocta.pycharm.odoo.python.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtils;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyCallExpressionImpl;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import dev.ngocta.pycharm.odoo.python.model.OdooSuperModelClassType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooPyCallExpression extends PyCallExpressionImpl {
    public OdooPyCallExpression(ASTNode astNode) {
        super(astNode);
    }

    @Nullable
    @Override
    public PyType getType(@NotNull TypeEvalContext context,
                          @NotNull TypeEvalContext.Key key) {
        PyExpression callee = getCallee();
        if (callee instanceof PyReferenceExpression) {
            if (PyNames.SUPER.equals(callee.getText())) {
                PyType type = getOdooSuperModelClassType(context);
                if (type != null) {
                    return type;
                }
            }
        }
        PyType type = super.getType(context, key);
        OdooModelClassType modelClassType = OdooModelUtils.extractOdooModelClassType(type);
        if (modelClassType != null) {
            return modelClassType;
        }
        return type;
    }

    private PyType getOdooSuperModelClassType(TypeEvalContext context) {
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
            return new OdooSuperModelClassType(modelClassType, anchor);
        }
        return null;
    }
}
