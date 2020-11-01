package dev.ngocta.pycharm.odoo.python;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.ObjectUtils;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.stubs.PyClassAttributesIndex;
import com.jetbrains.python.psi.types.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class OdooPyUtils {
    private OdooPyUtils() {
    }

    @Nullable
    public static PyClass getClassByQName(@NotNull String name,
                                          @Nullable PsiElement anchor) {
        if (anchor == null) {
            return null;
        }
        PsiElement fileAnchor = ObjectUtils.notNull(anchor.getContainingFile(), anchor);
        return PyUtil.getNullableParameterizedCachedValue(fileAnchor, name, param -> {
            PyPsiFacade psiFacade = PyPsiFacade.getInstance(anchor.getProject());
            return psiFacade.createClassByQName(name, anchor);
        });
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
    public static PyCollectionType getListType(@NotNull List<PyType> members,
                                               boolean isDefinition,
                                               @NotNull PsiElement anchor) {
        PyClass cls = PyBuiltinCache.getInstance(anchor).getClass("list");
        if (cls != null) {
            return new PyCollectionTypeImpl(cls, isDefinition, members);
        }
        return null;
    }

    @Nullable
    public static PyClassType getEnvironmentType(@NotNull PsiElement anchor) {
        return getClassTypeByQName(OdooNames.ENVIRONMENT_CLASS_QNAME, anchor, false);
    }

    @Nullable
    public static PyClassType getDbCursorType(@NotNull PsiElement anchor) {
        return getClassTypeByQName(OdooNames.DB_CURSOR_CLASS_QNAME, anchor, false);
    }

    @Nullable
    public static PyClassType getRegistryType(@NotNull PsiElement anchor) {
        return getClassTypeByQName(OdooNames.REGISTRY_CLASS_QNAME, anchor, false);
    }

    @Nullable
    public static PyType extractCompositedType(@Nullable PyType type,
                                               @NotNull Predicate<PyType> matcher) {
        if (type == null) {
            return null;
        }
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

    public static boolean isEnvironmentType(@Nullable PyType type) {
        PyType extractedType = extractCompositedType(type, t -> {
            PyClassType classType = ObjectUtils.tryCast(t, PyClassType.class);
            return classType != null && OdooNames.ENVIRONMENT_CLASS_QNAME.equals(classType.getPyClass().getQualifiedName());
        });
        return extractedType != null;
    }

    public static boolean isEnvironmentTypeExpression(@Nullable PyExpression expression,
                                                      @NotNull TypeEvalContext context) {
        if (expression == null) {
            return false;
        }
        if ("env".equals(expression.getName()) && OdooUtils.isOdooCode(expression)) {
            return true;
        }
        PyType type = context.getType(expression);
        return isEnvironmentType(type);
    }

    public static Collection<PyTargetExpression> findClassAttributes(@NotNull String name,
                                                                     @NotNull Project project,
                                                                     @NotNull GlobalSearchScope scope) {
        List<PyTargetExpression> result = new ArrayList<>();
        StubIndex.getInstance().processElements(PyClassAttributesIndex.KEY, name, project, scope, PyClass.class, clazz -> {
            ProgressManager.checkCanceled();
            PyTargetExpression classAttr = clazz.findClassAttribute(name, false, null);
            if (classAttr != null) {
                result.add(classAttr);
            }
            return true;
        });
        return result;
    }

    public static boolean isUnknownType(@Nullable PyType type) {
        if (type == null) {
            return true;
        }
        if (type instanceof PyUnionType) {
            Collection<PyType> members = ((PyUnionType) type).getMembers();
            if (members.size() == 2 && members.contains(null) && members.stream().anyMatch(t -> t instanceof PyNoneType)) {
                return true;
            }
        }
        return type instanceof PyStructuralType && ((PyStructuralType) type).isInferredFromUsages();
    }

    @Nullable
    public static PyType getType(@Nullable PyTypedElement element) {
        if (element == null) {
            return null;
        }
        PsiFile file = element.getContainingFile();
        if (file == null) {
            return null;
        }
        TypeEvalContext context = TypeEvalContext.codeAnalysis(file.getProject(), file);
        PyType type = context.getType(element);
        if (type == null) {
            context = TypeEvalContext.userInitiated(file.getProject(), file);
            type = context.getType(element);
        }
        return type;
    }
}
