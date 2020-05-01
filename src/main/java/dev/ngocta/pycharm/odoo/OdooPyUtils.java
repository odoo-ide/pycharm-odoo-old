package dev.ngocta.pycharm.odoo;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyPsiFacade;
import com.jetbrains.python.psi.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class OdooPyUtils {
    private OdooPyUtils() {
    }

    @Nullable
    public static PyClass getClassByQName(@NotNull String name,
                                          @NotNull PsiElement anchor) {
        PyPsiFacade psiFacade = PyPsiFacade.getInstance(anchor.getProject());
        return psiFacade.createClassByQName(name, anchor);
    }

    @Nullable
    public static PyClassType getClassTypeByQName(@NotNull String name,
                                                  @NotNull PsiElement anchor,
                                                  boolean isDefinition) {
        PyClass cls = getClassByQName(name, anchor);
        if (cls != null) {
            return new PyClassTypeImpl(cls, isDefinition);
        }
        return null;
    }

    @Nullable
    public static PyClassType getDateType(@NotNull PsiElement anchor) {
        return getClassTypeByQName(PyNames.TYPE_DATE, anchor, false);
    }

    @Nullable
    public static PyClassType getDatetimeType(@NotNull PsiElement anchor) {
        return getClassTypeByQName(PyNames.TYPE_DATE_TIME, anchor, false);
    }

    @Nullable
    public static PyClassType getEnvironmentType(@NotNull PsiElement anchor) {
        return getClassTypeByQName(OdooNames.ENVIRONMENT_QNAME, anchor, false);
    }

    @Nullable
    public static PyType extractType(@NotNull PyType type,
                                     @NotNull Predicate<PyType> matcher) {
        if (type instanceof PyUnionType) {
            for (PyType member : ((PyUnionType) type).getMembers()) {
                if (matcher.test(member)) {
                    return member;
                }
            }
        } else if (matcher.test(type)) {
            return type;
        }
        return null;
    }

    public static boolean isEnvironmentType(@NotNull PyType type,
                                            @NotNull PsiElement anchor) {
        PyType envType = getEnvironmentType(anchor);
        return envType != null && extractType(type, envType::equals) != null;
    }

    public static boolean isEnvironmentTypeExpression(@NotNull PyExpression expression,
                                                      @NotNull TypeEvalContext context) {
        if ("env".equals(expression.getName())) {
            return true;
        }
        PyType type = context.getType(expression);
        return type != null && isEnvironmentType(type, expression);
    }
}
