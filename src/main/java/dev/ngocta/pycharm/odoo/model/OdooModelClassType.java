package dev.ngocta.pycharm.odoo.model;

import com.intellij.codeInsight.completion.BasicInsertHandler;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.codeInsight.completion.PyFunctionInsertHandler;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import com.jetbrains.python.psi.types.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class OdooModelClassType extends UserDataHolderBase implements PyClassType {
    private final OdooModelClass myClass;
    private final OdooRecordSetType myRecordSetType;
    private static final double COMPLETION_PRIORITY_FIELD = 2;
    private static final double COMPLETION_PRIORITY_FUNCTION = 1;

    public OdooModelClassType(@NotNull OdooModelClass source, @NotNull OdooRecordSetType recordSetType) {
        myClass = source;
        myRecordSetType = recordSetType;
    }

    public OdooModelClassType(@NotNull String model, @NotNull OdooRecordSetType recordSetType, @NotNull Project project) {
        this(OdooModelClass.create(model, project), recordSetType);
    }

    @Nullable
    public static OdooModelClassType create(@NotNull PyClass source, @NotNull OdooRecordSetType recordSetType) {
        if (source instanceof OdooModelClass) {
            return new OdooModelClassType((OdooModelClass) source, recordSetType);
        }
        OdooModelInfo info = OdooModelInfo.getInfo(source);
        if (info != null) {
            return new OdooModelClassType(info.getName(), recordSetType, source.getProject());
        }
        return null;
    }

    public OdooRecordSetType getRecordSetType() {
        return myRecordSetType;
    }

    public OdooModelClassType withOneRecord() {
        return new OdooModelClassType(myClass, OdooRecordSetType.ONE);
    }

    public OdooModelClassType withMultiRecord() {
        return new OdooModelClassType(myClass, OdooRecordSetType.MULTI);
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
                                                            @NotNull PyResolveContext resolveContext) {
        return resolveMember(name, location, direction, resolveContext, true);
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
        List<RatedResolveResult> result = new LinkedList<>();
        visitMembers(element -> {
            if (element instanceof PsiNamedElement && name.equals(((PsiNamedElement) element).getName())) {
                if (PyNames.GETITEM.equals(name)) {
                    element = new OdooModelGetItemWrapper((PyFunction) element, this);
                }
                result.add(new RatedResolveResult(RatedResolveResult.RATE_NORMAL, element));
            }
            return true;
        }, true, context);
        return result;
    }

    @Override
    public void visitMembers(@NotNull Processor<PsiElement> processor, boolean inherited,
                             @NotNull TypeEvalContext context) {
        if (inherited) {
            for (PyClass cls : myClass.getAncestorClasses(context)) {
                if (!cls.processClassLevelDeclarations((element, state) -> processor.process(element))) {
                    return;
                }
            }
            for (OdooModelClass cls : myClass.getDelegationChildren(context)) {
                if (!cls.visitField(processor::process, context)) {
                    return;
                }
            }
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
        List<PyClassLikeType> result = new LinkedList<>();
        myClass.getAncestorClasses(context).forEach(cls -> {
            result.add(new PyClassTypeImpl(cls, isDefinition()));
        });
        return result;
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
        return myRecordSetType == OdooRecordSetType.NONE;
    }

    @NotNull
    @Override
    public PyClassLikeType toInstance() {
        return myRecordSetType != null ? this : new OdooModelClassType(myClass, OdooRecordSetType.MODEL);
    }

    @NotNull
    @Override
    public PyClassLikeType toClass() {
        return myRecordSetType == null ? this : new OdooModelClassType(myClass, OdooRecordSetType.NONE);
    }

    @Override
    public Object[] getCompletionVariants(String completionPrefix, PsiElement location, ProcessingContext processingContext) {
        TypeEvalContext context = TypeEvalContext.codeCompletion(location.getProject(), location.getContainingFile());
        Map<String, Object> map = new LinkedHashMap<>();
        visitMembers(member -> {
            if (member instanceof PsiNamedElement) {
                String name = ((PsiNamedElement) member).getName();
                if (!map.containsKey(name)) {
                    map.put(name, createCompletionLine((PsiNamedElement) member, context));
                }
            }
            return true;
        }, true, context);
        return map.values().toArray();
    }

    @Nullable
    private LookupElement createCompletionLine(@NotNull PsiNamedElement element, @NotNull TypeEvalContext context) {
        String name = element.getName();
        if (name != null) {
            String tailText = null;
            String typeText = null;
            double priority = 0;
            InsertHandler<LookupElement> insertHandler = new BasicInsertHandler<>();
            if (element instanceof PyTargetExpression) {
                OdooFieldInfo info = OdooFieldInfo.getInfo((PyTargetExpression) element);
                if (info != null) {
                    typeText = info.getTypeName();
                    PyType type = info.getType(context);
                    if (type instanceof OdooModelClassType) {
                        typeText = "(" + type.getName() + ") " + typeText;
                    }
                    priority = COMPLETION_PRIORITY_FIELD;
                }
            } else if (element instanceof PyFunction) {
                List<PyCallableParameter> params = ((PyFunction) element).getParameters(context);
                String paramsText = StringUtil.join(params, PyCallableParameter::getName, ", ");
                tailText = "(" + paramsText + ")";
                priority = COMPLETION_PRIORITY_FUNCTION;
                insertHandler = PyFunctionInsertHandler.INSTANCE;
            }
            LookupElement lookupElement = LookupElementBuilder.create(element)
                    .withTailText(tailText)
                    .withTypeText(typeText)
                    .withIcon(element.getIcon(Iconable.ICON_FLAG_READ_STATUS))
                    .withInsertHandler(insertHandler);
            return PrioritizedLookupElement.withPriority(lookupElement, priority);
        }
        return null;
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
    public OdooModelClass getPyClass() {
        return myClass;
    }

    @Nullable
    public PyType getFieldTypeByPath(@NotNull String path, @NotNull TypeEvalContext context) {
        String[] fieldNames = path.split("\\.");
        return getFieldTypeByPath(Arrays.asList(fieldNames), context);
    }

    @Nullable
    public PyType getFieldTypeByPath(@NotNull List<String> fieldNames, @NotNull TypeEvalContext context) {
        if (fieldNames.isEmpty()) {
            return null;
        }
        PsiFile file = context.getOrigin();
        if (file == null) {
            return null;
        }
        PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(file);
        PyType intType = builtinCache.getIntType();
        boolean toId = OdooNames.FIELD_ID.equals(fieldNames.get(fieldNames.size() - 1));
        if (toId) {
            fieldNames = fieldNames.subList(0, fieldNames.size() - 1);
            if (fieldNames.isEmpty()) {
                return intType;
            }
        }
        PyTargetExpression field = myClass.findFieldByPath(fieldNames, context);
        if (field != null) {
            PyType fieldType = OdooFieldInfo.getFieldType(field, context);
            if (toId) {
                if (fieldType instanceof OdooModelClassType) {
                    return intType;
                }
                return null;
            }
            return fieldType;
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o != null && getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hash(myClass);
    }
}