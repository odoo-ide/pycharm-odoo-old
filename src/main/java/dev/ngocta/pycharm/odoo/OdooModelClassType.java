package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.ResolveResultList;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import com.jetbrains.python.psi.types.*;
import org.apache.log4j.lf5.viewer.TrackingAdjustmentListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class OdooModelClassType extends UserDataHolderBase implements PyClassType {
    private OdooModelClass myClass;
    private OdooRecordSetType myRecordSetType;

    @Nullable
    public static OdooModelClassType create(@NotNull PyClass source, boolean isDefinition) {
        return create(source, isDefinition ? null : OdooRecordSetType.MULTI);
    }

    @Nullable
    public static OdooModelClassType create(@NotNull PyClass source, @Nullable OdooRecordSetType recordSetType) {
        if (source instanceof OdooModelClass) {
            return new OdooModelClassType((OdooModelClass) source, recordSetType);
        }
        OdooModelInfo info = OdooModelInfo.readFromClass(source);
        if (info != null) {
            return new OdooModelClassType(OdooModelClass.get(info.getName(), source.getProject()), recordSetType);
        }
        return null;
    }

    public OdooModelClassType(@NotNull OdooModelClass source, OdooRecordSetType recordSetType) {
        myClass = source;
        myRecordSetType = recordSetType;
    }

    public OdooModelClass getModelClass() {
        return myClass;
    }

    public OdooRecordSetType getRecordSetType() {
        return myRecordSetType;
    }

    public OdooModelClassType getOneRecord() {
        return new OdooModelClassType(myClass, OdooRecordSetType.ONE);
    }

    @Nullable
    @Override
    public String getClassQName() {
        return myClass.getQualifiedName();
    }

    @NotNull
    @Override
    public List<PyClassLikeType> getSuperClassTypes(@NotNull TypeEvalContext context) {
        List<PyClassLikeType> result = new LinkedList<>();
        for (PyClass cls : myClass.getSuperClasses(context)) {
            if (cls instanceof OdooModelClass) {
                result.add(new OdooModelClassType((OdooModelClass) cls, myRecordSetType));
            } else {
                result.add(new PyClassTypeImpl(cls, isDefinition()));
            }
        }
        return result;
    }

    @Nullable
    @Override
    public List<? extends RatedResolveResult> resolveMember(@NotNull String name,
                                                            @Nullable PyExpression location,
                                                            @NotNull AccessDirection direction,
                                                            @NotNull PyResolveContext resolveContext,
                                                            boolean inherited) {
        if (!inherited) {
            return null;
        }
        TypeEvalContext context = resolveContext.getTypeEvalContext();
        for (PyClass cls : myClass.getAncestorClasses(context)) {
            PsiElement member = cls.findClassAttribute(name, false, context);
            if (member == null) {
                member = cls.findMethodByName(name, false, context);
            }
            if (member != null) {
                return ResolveResultList.to(member);
            }
        }
        return null;
    }

    @Override
    public void visitMembers(@NotNull Processor<PsiElement> processor, boolean inherited,
                             @NotNull TypeEvalContext context) {
        if (inherited) {
            myClass.getAncestorClasses(context).forEach(cls -> {
                cls.processClassLevelDeclarations((element, state) -> {
                    processor.process(element);
                    return true;
                });
            });
        }
    }

    @NotNull
    @Override
    public Set<String> getMemberNames(boolean inherited, @NotNull TypeEvalContext context) {
        Set<String> result = new HashSet<>();
        visitMembers(member -> {
            if (member instanceof PsiNamedElement) {
                result.add(((PsiNamedElement) member).getName());
            }
            return true;
        }, inherited, context);
        return result;
    }

    @Override
    public boolean isValid() {
        return myClass.isValid();
    }

    @Nullable
    @Override
    public PyClassLikeType getMetaClassType(@NotNull TypeEvalContext context, boolean inherited) {
        return null;
    }

    @NotNull
    @Override
    public List<PyClassLikeType> getAncestorTypes(@NotNull TypeEvalContext context) {
        return PyUtil.getParameterizedCachedValue(myClass, context, contextArg -> {
            List<PyClassLikeType> result = new LinkedList<>();
            myClass.getAncestorClasses(contextArg).forEach(cls -> {
                result.add(new PyClassTypeImpl(cls, isDefinition()));
            });
            return result;
        });
    }

    @Nullable
    @Override
    public PyType getReturnType(@NotNull TypeEvalContext context) {
        return null;
    }

    @Nullable
    @Override
    public PyType getCallType(@NotNull TypeEvalContext context, @NotNull PyCallSiteExpression pyCallSiteExpression) {
        return null;
    }

    @Override
    public boolean isDefinition() {
        return myRecordSetType == null;
    }

    @NotNull
    @Override
    public PyClassLikeType toInstance() {
        return myRecordSetType != null ? this : new OdooModelClassType(myClass, OdooRecordSetType.MODEL);
    }

    @NotNull
    @Override
    public PyClassLikeType toClass() {
        return myRecordSetType == null ? this : new OdooModelClassType(myClass, null);
    }

    @Nullable
    @Override
    public List<? extends RatedResolveResult> resolveMember(@NotNull String name,
                                                            @Nullable PyExpression location,
                                                            @NotNull AccessDirection direction,
                                                            @NotNull PyResolveContext resolveContext) {
        return resolveMember(name, location, direction, resolveContext, true);
    }

    @Override
    public Object[] getCompletionVariants(String completionPrefix, PsiElement location, ProcessingContext context) {
        Map<String, PsiElement> names = new LinkedHashMap<>();
        TypeEvalContext typeEvalContext = TypeEvalContext.codeCompletion(location.getProject(), location.getContainingFile());
        visitMembers(member -> {
            if (member instanceof PsiNamedElement) {
                String name = ((PsiNamedElement) member).getName();
                names.putIfAbsent(name, member);
            }
            return true;
        }, true, typeEvalContext);
        return names.values().toArray();
    }

    @NotNull
    @Override
    public String getName() {
        return myClass.getName();
    }

    @NotNull
    public Project getProject() {
        return myClass.getProject();
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    @Override
    public void assertValid(String message) {
        if (!isValid()) {
            throw new PsiInvalidElementAccessException(null, myClass.getName() + ": " + message);
        }
    }

    @NotNull
    @Override
    public PyClass getPyClass() {
        return myClass;
    }
}