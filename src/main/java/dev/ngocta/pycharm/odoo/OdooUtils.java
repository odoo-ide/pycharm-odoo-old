package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.*;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import dev.ngocta.pycharm.odoo.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OdooUtils {
    @Nullable
    public static VirtualFile getOdooModuleDir(@NotNull VirtualFile file) {
        VirtualFile cur = file;
        while (cur != null) {
            if (cur.findChild(OdooNames.__MANIFEST__DOT_PY) != null) {
                return cur;
            }
            cur = cur.getParent();
        }
        return null;
    }

    @Nullable
    public static PsiDirectory getOdooModuleDir(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        if (file != null) {
            VirtualFile virtualFile = getOdooModuleDir(file.getOriginalFile().getVirtualFile());
            if (virtualFile != null) {
                return PsiManager.getInstance(element.getProject()).findDirectory(virtualFile);
            }
        }
        return null;
    }

    public static boolean isOdooModelFile(@Nullable PsiFile file) {
        return file instanceof PyFile && getOdooModuleDir(file.getVirtualFile()) != null;
    }

    @Nullable
    public static PyClass getClassByQName(@NotNull String name, @NotNull PsiElement anchor) {
        Project project = anchor.getProject();
        ConcurrentMap<String, PyClass> cache = CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            return CachedValueProvider.Result.create(new ConcurrentHashMap<>(), ModificationTracker.NEVER_CHANGED);
        });
        PyClass cls = cache.get(name);
        if (cls == null) {
            PyPsiFacade psiFacade = PyPsiFacade.getInstance(project);
            cls = psiFacade.createClassByQName(name, anchor);
            cache.put(name, cls);
        }
        return cls;
    }

    @Nullable
    public static PyFunction findMethodByName(@NotNull String name, @NotNull PyClass pyClass) {
        PsiElement member = findClassMember(name, pyClass);
        if (member instanceof PyFunction) {
            return (PyFunction) member;
        }
        return null;
    }

    @Nullable
    public static PyTargetExpression findClassAttribute(@NotNull String name, @NotNull PyClass pyClass) {
        PsiElement member = findClassMember(name, pyClass);
        if (member instanceof PyTargetExpression) {
            return (PyTargetExpression) member;
        }
        return null;
    }

    @Nullable
    public static PsiElement findClassMember(@NotNull String name, @NotNull PyClass pyClass) {
        Map<String, PsiElement> cache = CachedValuesManager.getCachedValue(pyClass, () -> {
            Map<String, PsiElement> members = new HashMap<>();
            pyClass.processClassLevelDeclarations((element, state) -> {
                if (element instanceof PsiNamedElement) {
                    members.put(((PsiNamedElement) element).getName(), element);
                }
                return true;
            });
            return CachedValueProvider.Result.create(members, pyClass);
        });
        return cache.get(name);
    }

    @Nullable
    public static PyType getFieldType(@NotNull PyTargetExpression field, @NotNull TypeEvalContext context) {
        OdooFieldInfo info = OdooFieldInfo.get(field, context);
        if (info == null) {
            return null;
        }
        Project project = field.getProject();
        PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(field);
        switch (info.getClassName()) {
            case OdooNames.MANY2ONE:
            case OdooNames.ONE2MANY:
            case OdooNames.MANY2MANY:
                if (info.getComodel() != null) {
                    OdooRecordSetType recordSetType = OdooNames.MANY2ONE.equals(info.getClassName()) ? OdooRecordSetType.ONE : OdooRecordSetType.MULTI;
                    return new OdooModelClassType(info.getComodel(), recordSetType, project);
                } else if (info.getRelated() != null) {
                    OdooModelClass modelClass = getContainingOdooModelClass(field, project);
                    if (modelClass != null) {
                        PyTargetExpression relatedField = modelClass.findFieldByPath(info.getRelated(), context);
                        if (relatedField != null) {
                            return getFieldType(relatedField, context);
                        }
                    }
                }
                return null;
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
                return builtinCache.getStrType();
            case OdooNames.DATE:
                return getDateType(field);
            case OdooNames.DATETIME:
                return getDatetimeType(field);
            default:
                return null;
        }
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
        return getClassTypeByQName(OdooNames.ENVIRONMENT_QNAME, anchor, false);
    }

    @NotNull
    public static PyClassType getContextType(@NotNull PsiElement anchor) {
        PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(anchor);
        return builtinCache.getDictType();
    }

    @Nullable
    public static PyClassType getDbCursorType(@NotNull PsiElement anchor) {
        return getClassTypeByQName(OdooNames.DB_CURSOR_QNAME, anchor, false);
    }

    @Nullable
    public static <T extends PyType> T unpackType(PyType type, Class<T> unpackedTypeClass) {
        if (unpackedTypeClass.isInstance(type)) {
            return unpackedTypeClass.cast(type);
        }
        if (type instanceof PyUnionType) {
            for (PyType member : ((PyUnionType) type).getMembers()) {
                if (unpackedTypeClass.isInstance(member)) {
                    return unpackedTypeClass.cast(member);
                }
            }
        }
        return null;
    }

    @Nullable
    public static OdooModelClass getContainingOdooModelClass(@NotNull PyPossibleClassMember member, @NotNull Project project) {
        PyClass cls = member.getContainingClass();
        if (cls != null) {
            OdooModelInfo info = OdooModelInfo.readFromClass(cls);
            if (info != null) {
                return OdooModelClass.create(info.getName(), project);
            }
        }
        return null;
    }
}
