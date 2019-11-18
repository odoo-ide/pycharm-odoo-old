package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import com.jetbrains.python.psi.types.PyClassLikeType;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyClassTypeImpl;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class OdooModelClassType extends PyClassTypeImpl {
    private OdooModelInfo myModelInfo;
    private OdooRecordSetType myRecordSetType;
    private List<PyClassLikeType> cachedAncestorTypes;

    @Nullable
    public static OdooModelClassType create(@NotNull PyClass source, boolean isDefinition) {
        return create(source, isDefinition ? null : OdooRecordSetType.MULTI);
    }

    @Nullable
    public static OdooModelClassType create(@NotNull PyClass source, @Nullable OdooRecordSetType recordSetType) {
        OdooModelInfo info = OdooModelInfo.readFromClass(source);
        if (info != null) {
            return new OdooModelClassType(source, info, recordSetType);
        }
        return null;
    }

    private OdooModelClassType(@NotNull PyClass source, @NotNull OdooModelInfo modelInfo, OdooRecordSetType recordSetType) {
        super(source, recordSetType == null);
        myModelInfo = modelInfo;
        myRecordSetType = recordSetType;
    }

    public OdooModelClassType getOneRecord() {
        return new OdooModelClassType(myClass, myModelInfo, OdooRecordSetType.ONE);
    }

    @NotNull
    @Override
    public List<PyClassLikeType> getSuperClassTypes(@NotNull TypeEvalContext context) {
        if (myModelInfo.getInherit().isEmpty()) {
            return super.getSuperClassTypes(context);
        }
        return getSuperClassTypes(context, Collections.emptySet());
    }

    @NotNull
    private List<PyClassLikeType> getSuperClassTypes(@NotNull TypeEvalContext context, @NotNull Set<PyClass> excludedClasses) {
        List<PyClassLikeType> result = new LinkedList<>();
        List<PyClass> supers = getSuperClasses(excludedClasses);
        supers.forEach(pyClass -> {
            OdooModelClassType superType = create(pyClass, myRecordSetType);
            if (superType != null) {
                result.add(superType);
            }
        });
        return result;
    }

    @NotNull
    @Override
    public List<PyClassLikeType> getAncestorTypes(@NotNull TypeEvalContext context) {
        if (cachedAncestorTypes != null) {
            return cachedAncestorTypes;
        }
        List<PyClassLikeType> result = new LinkedList<>();
        Set<PyClass> excludedClasses = new HashSet<>();
        excludedClasses.add(myClass);
        resolveAncestorTypes(this, context, result, excludedClasses);
        cachedAncestorTypes = result;
        return result;
    }

    private void resolveAncestorTypes(OdooModelClassType type, TypeEvalContext context, List<PyClassLikeType> result,
                                      Set<PyClass> excludedClasses) {
        type.getSuperClassTypes(context, excludedClasses).forEach(pyClassLikeType -> {
            if (pyClassLikeType instanceof OdooModelClassType) {
                OdooModelClassType odooModelClassType = (OdooModelClassType) pyClassLikeType;
                result.add(odooModelClassType);
                excludedClasses.add(odooModelClassType.getPyClass());
                resolveAncestorTypes(odooModelClassType, context, result, excludedClasses);
            }
        });
    }

    @NotNull
    private List<PyClass> getSuperClasses(@NotNull Set<PyClass> excludedClasses) {
        List<PyClass> result = new LinkedList<>();
        myModelInfo.getInherit().forEach(s -> {
            resolveSuperClasses(s, myModelInfo.getModuleName(), result, excludedClasses);
        });
        return result;
    }

    private void resolveSuperClasses(@NotNull String model, @NotNull String moduleName, @NotNull List<PyClass> result,
                                     @NotNull Set<PyClass> excludedClasses) {
        Project project = myClass.getProject();
        List<PyClass> pyClasses = OdooModelIndex.findModelClasses(model, moduleName, project);
        pyClasses.removeAll(excludedClasses);
        if (pyClasses.isEmpty()) {
            List<String> depends = OdooModuleIndex.getDepends(moduleName, project);
            depends.forEach(depend -> resolveSuperClasses(model, depend, result, excludedClasses));
        } else if (pyClasses.stream().noneMatch(result::contains)) {
            result.addAll(pyClasses);
        }
    }

    @Nullable
    private PsiElement findMember(@NotNull String name, @NotNull TypeEvalContext context, boolean inherit) {
        PyTargetExpression attExpr = myClass.findClassAttribute(name, false, context);
        if (attExpr != null) {
            return attExpr;
        }
        PyFunction funcExpr = myClass.findMethodByName(name, false, context);
        if (funcExpr != null) {
            return funcExpr;
        }
        if (inherit) {
            for (PyClassLikeType classType : getAncestorTypes(context)) {
                if (classType instanceof OdooModelClassType) {
                    PsiElement member = ((OdooModelClassType) classType).findMember(name, context, false);
                    if (member != null) {
                        return member;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public List<? extends RatedResolveResult> resolveMember(@NotNull String name,
                                                            @Nullable PyExpression location,
                                                            @NotNull AccessDirection direction,
                                                            @NotNull PyResolveContext resolveContext,
                                                            boolean inherited) {
        PsiElement element = findMember(name, resolveContext.getTypeEvalContext(), inherited);
        if (element != null) {
            return Collections.singletonList(new RatedResolveResult(RatedResolveResult.RATE_NORMAL, element));
        }
        return super.resolveMember(name, location, direction, resolveContext, inherited);
    }

    @Override
    public @Nullable String getName() {
        return myModelInfo.getName();
    }
}