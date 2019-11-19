package dev.ngocta.pycharm.odoo;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import com.jetbrains.python.psi.types.PyClassLikeType;
import com.jetbrains.python.psi.types.PyClassTypeImpl;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class OdooModelClassType extends PyClassTypeImpl {
    private OdooModelInfo myModelInfo;
    private OdooRecordSetType myRecordSetType;
    private List<PyClassLikeType> cachedAncestorTypes;
    private static final ThreadLocal<Set<PyClass>> resolvedClassStack = ThreadLocal.withInitial(HashSet::new);

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
        List<PyClassLikeType> result = new LinkedList<>();
        List<PyClass> supers = getSuperClasses();
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
        doGetAncestorTypes(context, result);
        cachedAncestorTypes = result;
        return result;
    }

    private void doGetAncestorTypes(TypeEvalContext context, List<PyClassLikeType> result) {
        Set<PyClass> resolvedClasses = resolvedClassStack.get();
        resolvedClasses.add(myClass);
        try {
            getSuperClassTypes(context).forEach(pyClassLikeType -> {
                if (pyClassLikeType instanceof OdooModelClassType) {
                    OdooModelClassType odooModelClassType = (OdooModelClassType) pyClassLikeType;
                    result.add(odooModelClassType);
                    odooModelClassType.doGetAncestorTypes(context, result);
                }
            });
        } finally {
            resolvedClasses.remove(myClass);
        }
    }

    @NotNull
    private List<PyClass> getSuperClasses() {
        List<PyClass> result = new LinkedList<>();
        myModelInfo.getInherit().forEach(s -> {
            doGetSuperClasses(s, myModelInfo.getModule(), result);
        });
        return result;
    }

    private void doGetSuperClasses(@NotNull String model, @NotNull PsiDirectory module, @NotNull List<PyClass> result) {
        List<PyClass> pyClasses = OdooModelIndex.findModelClasses(model, module);
        pyClasses.removeAll(resolvedClassStack.get());
        if (pyClasses.isEmpty()) {
            OdooModuleIndex.getDepends(module).forEach(depend -> doGetSuperClasses(model, depend, result));
        } else {
            pyClasses.forEach(pyClass -> {
                if (!result.contains(pyClass)) {
                    result.add(pyClass);
                }
            });
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

    @NotNull
    @Override
    public Object[] getCompletionVariants(String prefix, PsiElement location, @NotNull ProcessingContext context) {
        Set<PyClass> resolvedClasses = resolvedClassStack.get();
        resolvedClasses.add(myClass);
        try {
            return super.getCompletionVariants(prefix, location, context);
        } finally {
            resolvedClasses.remove(myClass);
        }
    }
}