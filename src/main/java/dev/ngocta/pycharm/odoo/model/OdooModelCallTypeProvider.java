package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.Ref;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooModelCallTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getCallType(@NotNull PyFunction function, @NotNull PyCallSiteExpression callSite, @NotNull TypeEvalContext context) {
        PyExpression receiver = callSite.getReceiver(function);
        if (receiver != null) {
            PyType receiverType = context.getType(receiver);
            if (receiver instanceof PyCallExpression) {
                PyExpression callee = ((PyCallExpression) receiver).getCallee();
                if (callee instanceof PyReferenceExpression && PyNames.SUPER.equals(callee.getName())) {
                    PyExpression arg = ((PyCallExpression) receiver).getArgument(1, PyExpression.class);
                    if (arg != null) {
                        PyType argType = context.getType(arg);
                        if (argType instanceof OdooModelClassType) {
                            receiverType = argType;
                        }
                    }
                }
            }
            if (receiverType instanceof OdooModelClassType) {
                PyType returnType = context.getReturnType(function);
                Ref<PyType> ref = new Ref<>(returnType);
                if (returnType instanceof PyClassType) {
                    PyClass containingClass = function.getContainingClass();
                    PyClass returnClass = ((PyClassType) returnType).getPyClass();
                    if (returnClass.equals(containingClass) || returnClass.equals(OdooModelUtils.getContainingOdooModelClass(containingClass))) {
                        List<PyClass> receiverAncestors = ((OdooModelClassType) receiverType).getPyClass().getAncestorClasses(context);
                        if (receiverAncestors.contains(containingClass)) {
                            ref.set(receiverType);
                        }
                    }
                }
                return ref;
            }
        }
        return null;
    }
}
