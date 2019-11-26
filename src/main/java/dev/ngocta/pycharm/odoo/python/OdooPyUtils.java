package dev.ngocta.pycharm.odoo.python;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyUnionType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.python.model.OdooRecordSetType;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OdooPyUtils {
    @Nullable
    public static VirtualFile getOdooModuleDir(@NotNull VirtualFile file) {
        VirtualFile cur = file;
        while (cur != null) {
            if (cur.findChild(OdooPyNames.__MANIFEST__DOT_PY) != null) {
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
            VirtualFile virtualFile = getOdooModuleDir(file.getVirtualFile());
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
    public static PyClass createClassByQName(@NotNull String name, @NotNull PsiElement anchor) {
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
    public static PyType getModelFieldType(@NotNull PyTargetExpression field, @NotNull TypeEvalContext context) {
        return CachedValuesManager.getCachedValue(field, () -> {
            PyType type = null;
            Project project = field.getProject();
            PyExpression assignedValue = field.findAssignedValue();
            if (assignedValue instanceof PyCallExpression) {
                PyCallExpression callExpression = (PyCallExpression) assignedValue;
                PyExpression callee = callExpression.getCallee();
                if (callee != null) {
                    String calleeName = callee.getName();
                    PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(field);
                    if (calleeName != null) {
                        switch (calleeName) {
                            case OdooPyNames.MANY2ONE:
                            case OdooPyNames.ONE2MANY:
                            case OdooPyNames.MANY2MANY:
                                PyStringLiteralExpression comodelExpression = callExpression.getArgument(
                                        0, OdooPyNames.COMODEL_NAME, PyStringLiteralExpression.class);
                                if (comodelExpression != null) {
                                    String comodel = comodelExpression.getStringValue();
                                    OdooRecordSetType recordSetType = calleeName.equals(OdooPyNames.MANY2ONE) ? OdooRecordSetType.ONE : OdooRecordSetType.MULTI;
                                    type = OdooModelClassType.create(comodel, recordSetType, project);
                                }
                                break;
                            case OdooPyNames.BOOLEAN:
                                type = builtinCache.getBoolType();
                                break;
                            case OdooPyNames.INTEGER:
                                type = builtinCache.getIntType();
                                break;
                            case OdooPyNames.FLOAT:
                            case OdooPyNames.MONETARY:
                                type = builtinCache.getFloatType();
                                break;
                            case OdooPyNames.CHAR:
                            case OdooPyNames.TEXT:
                            case OdooPyNames.SELECTION:
                                type = PyUnionType.union(builtinCache.getStrType(), null);
                                break;
                            case OdooPyNames.DATE:
                                PyClass dateClass = OdooPyUtils.createClassByQName("datetime.date", field);
                                if (dateClass != null) {
                                    type = PyUnionType.union(context.getType(dateClass), null);
                                }
                                break;
                            case OdooPyNames.DATETIME:
                                PyClass datetimeClass = OdooPyUtils.createClassByQName("datetime.datetime", field);
                                if (datetimeClass != null) {
                                    type = PyUnionType.union(context.getType(datetimeClass), null);
                                }
                                break;
                        }
                    }
                }
            }
            return CachedValueProvider.Result.createSingleDependency(type, field);
        });
    }
}
