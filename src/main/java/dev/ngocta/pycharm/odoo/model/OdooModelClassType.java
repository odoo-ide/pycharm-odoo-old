package dev.ngocta.pycharm.odoo.model;

import com.intellij.codeInsight.completion.BasicInsertHandler;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.fileTypes.FileTypeEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiInvalidElementAccessException;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import com.intellij.util.Processor;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.codeInsight.completion.PyFunctionInsertHandler;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.resolve.ImplicitResolveResult;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import com.jetbrains.python.psi.types.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooTypeUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Stream;


public class OdooModelClassType extends UserDataHolderBase implements PyCollectionType {
    private final OdooModelClass myClass;
    private final OdooRecordSetType myRecordSetType;
    private static final double FIELD_PRIORITY = 2;
    private static final double FUNCTION_PRIORITY = 1;

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
        if (OdooNames.BASE_MODEL_CLASS_QNAME.equals(source.getQualifiedName())) {
            return new OdooModelClassType("", recordSetType, source.getProject());
        }
        OdooModelInfo info = OdooModelInfo.readFromClass(source);
        if (info != null) {
            return new OdooModelClassType(info.getName(), recordSetType, source.getProject());
        }
        return null;
    }

    public OdooRecordSetType getRecordSetType() {
        return myRecordSetType;
    }

    public OdooModelClassType getOneRecordVariant() {
        return new OdooModelClassType(myClass, OdooRecordSetType.ONE);
    }

    public OdooModelClassType getMultiRecordVariant() {
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
        List<RatedResolveResult> result = new LinkedList<>();
        TypeEvalContext context = resolveContext.getTypeEvalContext();
        visitMembers(element -> {
            if (element instanceof PsiNamedElement && name.equals(((PsiNamedElement) element).getName())) {
                if (element instanceof PyFunction) {
                    element = OdooModelFunction.wrapIfNeeded((PyFunction) element, this);
                }
                result.add(new ImplicitResolveResult(element, RatedResolveResult.RATE_NORMAL));
            }
            return true;
        }, true, context);
        if (result.isEmpty() && getImplicitAttributeNames(context).contains(name)) {
            result.add(new ImplicitResolveResult(null, RatedResolveResult.RATE_NORMAL));
        }
        return result;
    }

    @Override
    public void visitMembers(@NotNull Processor<PsiElement> processor, boolean inherited,
                             @NotNull TypeEvalContext context) {
        if (inherited) {
            Stream.concat(
                    myClass.getAncestorClasses(context).stream(),
                    myClass.getDelegationChildren(context).stream()).forEach(cls -> {
                cls.processClassLevelDeclarations((element, state) -> {
                    return processor.process(element);
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
        List<LookupElement> implicitAttributeLines = getImplicitAttributeCompletionLines(context);
        List<Object> lines = new LinkedList<>(implicitAttributeLines);
        Set<String> names = new HashSet<>(getImplicitAttributeNames(context));
        visitMembers(member -> {
            if (member instanceof PsiNamedElement) {
                String name = ((PsiNamedElement) member).getName();
                if (!names.contains(name)) {
                    lines.add(getCompletionLine((PsiNamedElement) member, context));
                    names.add(name);
                }
            }
            return true;
        }, true, context);
        myClass.getDelegationChildren(context).forEach(child -> {
            child.visitClassAttributes(attr -> {
                if (OdooFieldInfo.getFieldType(attr, context) != null) {
                    String name = attr.getName();
                    if (!names.contains(name)) {
                        lines.add(getCompletionLine(attr, context));
                        names.add(name);
                    }
                }
                return true;
            }, true, context);
        });
        return lines.toArray();
    }

    public Collection<String> getImplicitAttributeNames(@NotNull TypeEvalContext context) {
        Collection<String> result = getMagicFieldNames(context);
        result.add(OdooNames.ENV);
        result.add(OdooNames.MODEL_CONTEXT);
        result.add(OdooNames.MODEL_CR);
        result.add(OdooNames.MODEL_UID);
        result.add(OdooNames.MODEL_POOL);
        result.add(OdooNames.MODEL_FIELDS);
        return result;
    }

    public Collection<String> getMagicFieldNames(@NotNull TypeEvalContext context) {
        List<String> result = new LinkedList<>();
        result.add(OdooNames.FIELD_ID);
        result.add(OdooNames.FIELD_DISPLAY_NAME);
        if (isEnableLogAccess()) {
            result.add(OdooNames.FIELD_CREATE_DATE);
            result.add(OdooNames.FIELD_CREATE_UID);
            result.add(OdooNames.FIELD_WRITE_DATE);
            result.add(OdooNames.FIELD_WRITE_UID);
        }
        return result;
    }

    public Map<String, PyType> getImplicitAttributeTypes(@NotNull TypeEvalContext context) {
        Map<String, PyType> result = new HashMap<>();
        PsiFile file = context.getOrigin();
        if (file != null) {
            PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(file);
            getImplicitAttributeNames(context).forEach(field -> {
                switch (field) {
                    case OdooNames.FIELD_ID:
                    case OdooNames.MODEL_UID:
                        result.put(field, builtinCache.getIntType());
                        break;
                    case OdooNames.FIELD_DISPLAY_NAME:
                        result.put(field, builtinCache.getStrType());
                        break;
                    case OdooNames.FIELD_CREATE_DATE:
                    case OdooNames.FIELD_WRITE_DATE:
                        result.put(field, OdooTypeUtils.getDatetimeType(file));
                        break;
                    case OdooNames.FIELD_CREATE_UID:
                    case OdooNames.FIELD_WRITE_UID:
                        result.put(field, new OdooModelClassType(OdooNames.RES_USERS, OdooRecordSetType.MULTI, getProject()));
                        break;
                    case OdooNames.ENV:
                        result.put(field, OdooTypeUtils.getEnvironmentType(file));
                        break;
                    case OdooNames.MODEL_CONTEXT:
                        result.put(field, OdooTypeUtils.getContextType(file));
                        break;
                    case OdooNames.MODEL_CR:
                        result.put(field, OdooTypeUtils.getDbCursorType(file));
                        break;
                    case OdooNames.MODEL_POOL:
                        result.put(field, OdooTypeUtils.getClassTypeByQName(OdooNames.REGISTRY_CLASS_QNAME, file, false));
                }
            });
        }
        return result;
    }

    private boolean isEnableLogAccess() {
        // TODO: check _log_access
        return true;
    }

    @NotNull
    private List<LookupElement> getImplicitAttributeCompletionLines(@NotNull TypeEvalContext context) {
        List<LookupElement> result = new LinkedList<>();
        getImplicitAttributeTypes(context).forEach((name, type) -> {
            String typeText = null;
            if (type instanceof PyClassType) {
                String typeName = ((PyClassType) type).getClassQName();
                if (typeName != null) {
                    switch (typeName) {
                        case PyNames.TYPE_INT:
                            typeText = OdooNames.FIELD_TYPE_INTEGER;
                            break;
                        case PyNames.TYPE_DATE:
                            typeText = OdooNames.FIELD_TYPE_DATE;
                            break;
                        case PyNames.TYPE_DATE_TIME:
                            typeText = OdooNames.FIELD_TYPE_DATETIME;
                            break;
                        case PyNames.TYPE_STR:
                            typeText = OdooNames.FIELD_TYPE_CHAR;
                            break;
                    }
                }
            }
            if (typeText == null && type != null) {
                typeText = type.getName();
            }
            LookupElement lookupElement = LookupElementBuilder.create(name)
                    .withTypeText(typeText)
                    .withIcon(PlatformIcons.FIELD_ICON);
            result.add(PrioritizedLookupElement.withPriority(lookupElement, FIELD_PRIORITY));
        });
        return result;
    }

    @Nullable
    private LookupElement getCompletionLine(@NotNull PsiNamedElement element, @NotNull TypeEvalContext context) {
        String name = element.getName();
        if (name != null) {
            String tailText = null;
            String typeText = null;
            double priority = 0;
            InsertHandler<LookupElement> insertHandler = new BasicInsertHandler<>();
            if (element instanceof PyTargetExpression) {
                OdooFieldInfo info = OdooFieldInfo.getInfo((PyTargetExpression) element, context);
                PyType type = OdooFieldInfo.getFieldType((PyTargetExpression) element, context);
                if (info != null) {
                    typeText = info.getClassName();
                    if (type instanceof OdooModelClassType) {
                        typeText = "(" + type.getName() + ") " + typeText;
                    }
                    priority = FIELD_PRIORITY;
                }
            } else if (element instanceof PyFunction) {
                List<PyCallableParameter> params = ((PyFunction) element).getParameters(context);
                String paramsText = StringUtil.join(params, PyCallableParameter::getName, ", ");
                tailText = "(" + paramsText + ")";
                priority = FUNCTION_PRIORITY;
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

    @NotNull
    @Override
    public List<PyType> getElementTypes() {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public PyType getIteratedItemType() {
        if (myRecordSetType != OdooRecordSetType.NONE) {
            return getOneRecordVariant();
        }
        return null;
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