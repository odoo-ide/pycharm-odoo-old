package dev.ngocta.pycharm.odoo;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyClassTypeImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyUnionType;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.function.Predicate;

public class OdooTypeUtils {
    private OdooTypeUtils() {
    }

    @Nullable
    public static PyClassType getClassTypeByQName(@NotNull String name, @NotNull PsiElement anchor, boolean isDefinition) {
        PyClass cls = OdooUtils.getClassByQName(name, anchor);
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
        return getClassTypeByQName(OdooNames.ENVIRONMENT_CLASS_QNAME, anchor, false);
    }

    @NotNull
    public static PyClassType getContextType(@NotNull PsiElement anchor) {
        PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(anchor);
        return builtinCache.getDictType();
    }

    @Nullable
    public static PyClassType getDbCursorType(@NotNull PsiElement anchor) {
        return getClassTypeByQName(OdooNames.DB_CURSOR_CLASS_QNAME, anchor, false);
    }

    @Nullable
    public static PyType extractType(@NotNull PyType type, @NotNull Predicate<PyType> matcher) {
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

    public static boolean isEnvironmentType(@Nullable PyType type, @NotNull PsiElement anchor) {
        if (type == null) {
            return false;
        }
        PyType envType = getEnvironmentType(anchor);
        return envType != null && extractType(type, envType::equals) != null;
    }
}
