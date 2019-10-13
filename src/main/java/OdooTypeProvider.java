import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyPsiFacadeImpl;
import com.jetbrains.python.psi.impl.PyTypeProvider;
import com.jetbrains.python.psi.types.*;
import org.apache.commons.collections.map.HashedMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class OdooTypeProvider implements PyTypeProvider {
    @Override
    public @Nullable PyType getReferenceExpressionType(@NotNull PyReferenceExpression pyReferenceExpression, @NotNull TypeEvalContext typeEvalContext) {
        PsiPolyVariantReference ref = pyReferenceExpression.getReference();
        ResolveResult[] results = ref.multiResolve(false);
        PsiElement target = null;
        if (results.length > 0) {
            target = results[0].getElement();
        }
        if (target == null) {
            return null;
        }
        if (!(target instanceof PyTargetExpression)) {
            return null;
        }
        QualifiedName callee = ((PyTargetExpression) target).getCalleeName();
        if (callee == null) {
            return null;
        }
        List<String> components = callee.getComponents();
        if (components.size() == 2 && components.get(0).equals("fields")) {
            PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(pyReferenceExpression);
            PyPsiFacade pyPsiFacade = PyPsiFacadeImpl.getInstance(pyReferenceExpression.getProject());
            PyClass pyClass;
            switch (components.get(1)) {
                case "Integer":
                    return builtinCache.getIntType();
                case "Float":
                    return builtinCache.getFloatType();
                case "Boolean":
                    return builtinCache.getBoolType();
                case "Char":
                case "Text":
                    return builtinCache.getStrType();
                case "Date":
                    pyClass = pyPsiFacade.createClassByQName("datetime.date", pyReferenceExpression);
                    if (pyClass != null) {
                        return new PyClassTypeImpl(pyClass, false);
                    }
                case "Datetime":
                    pyClass = pyPsiFacade.createClassByQName("datetime.datetime", pyReferenceExpression);
                    if (pyClass != null) {
                        return new PyClassTypeImpl(pyClass, false);
                    }
            }
        }
        return null;
    }

    @Override
    public @Nullable Ref<PyType> getReferenceType(@NotNull PsiElement psiElement, @NotNull TypeEvalContext typeEvalContext, @Nullable PsiElement psiElement1) {
        return null;
    }

    @Override
    public @Nullable Ref<PyType> getParameterType(@NotNull PyNamedParameter pyNamedParameter, @NotNull PyFunction pyFunction, @NotNull TypeEvalContext typeEvalContext) {
        return null;
    }

    @Override
    public @Nullable Ref<PyType> getReturnType(@NotNull PyCallable pyCallable, @NotNull TypeEvalContext typeEvalContext) {
        return null;
    }

    @Override
    public @Nullable Ref<PyType> getCallType(@NotNull PyFunction pyFunction, @NotNull PyCallSiteExpression pyCallSiteExpression, @NotNull TypeEvalContext typeEvalContext) {
        return null;
    }

    @Override
    public @Nullable PyType getContextManagerVariableType(PyClass pyClass, PyExpression pyExpression, TypeEvalContext typeEvalContext) {
        return null;
    }

    @Override
    public @Nullable PyType getCallableType(@NotNull PyCallable pyCallable, @NotNull TypeEvalContext typeEvalContext) {
        return null;
    }

    @Override
    public @Nullable PyType getGenericType(@NotNull PyClass pyClass, @NotNull TypeEvalContext typeEvalContext) {
        return null;
    }

    @Override
    public @NotNull Map<PyType, PyType> getGenericSubstitutions(@NotNull PyClass pyClass, @NotNull TypeEvalContext typeEvalContext) {
        return Collections.emptyMap();
    }
}
