package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ObjectUtils;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.python.OdooPyUtils;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class OdooFieldInfo {
    private final String myName;
    private final PsiElement myElement;
    private final String myTypeName;
    private final Map<String, Object> myAttributes;

    public OdooFieldInfo(@NotNull String name,
                         @Nullable PsiElement element,
                         @NotNull String typeName,
                         @NotNull Map<String, Object> attributes) {
        myName = name;
        myElement = element;
        myTypeName = typeName;
        myAttributes = attributes;
    }

    @NotNull
    public String getName() {
        return myName;
    }

    @NotNull
    public String getTypeName() {
        return myTypeName;
    }

    @Nullable
    public String getComodel() {
        return doGetComodel(new THashSet<>());
    }

    private String doGetComodel(Set<PsiElement> visitedFields) {
        if (visitedFields.contains(myElement)) {
            return null;
        }
        visitedFields.add(myElement);
        String comodel = ObjectUtils.tryCast(myAttributes.get(OdooNames.FIELD_ATTR_COMODEL_NAME), String.class);
        if (comodel == null) {
            OdooFieldInfo relatedInfo = getRelatedFieldInfo();
            if (relatedInfo != null) {
                comodel = relatedInfo.doGetComodel(visitedFields);
            }
        }
        return comodel;
    }

    @Nullable
    public String getRelated() {
        return ObjectUtils.tryCast(myAttributes.get(OdooNames.FIELD_ATTR_RELATED), String.class);
    }

    @Nullable
    public PsiElement getRelatedField() {
        String related = getRelated();
        if (related == null || myName.equals(related)) {
            return null;
        }
        OdooModelClass cls = OdooModelUtils.getContainingOdooModelClass(myElement);
        if (cls == null) {
            return null;
        }
        TypeEvalContext context = TypeEvalContext.userInitiated(myElement.getProject(), myElement.getContainingFile());
        return cls.findFieldByPath(related, context);
    }

    @Nullable
    public OdooFieldInfo getRelatedFieldInfo() {
        PsiElement relatedField = getRelatedField();
        return getInfo(relatedField);
    }

    public boolean isDelegate() {
        Object value = myAttributes.get(OdooNames.FIELD_ATTR_DELEGATE);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return false;
    }

    @Nullable
    public static OdooFieldInfo getInfo(@Nullable PsiElement field) {
        if (field instanceof PyTargetExpression) {
            return CachedValuesManager.getCachedValue(field, () -> {
                OdooFieldInfo info = getInfoInner((PyTargetExpression) field);
                return CachedValueProvider.Result.create(info, field);
            });
        }
        return null;
    }

    @Nullable
    private static OdooFieldInfo getInfoInner(@NotNull PyTargetExpression field) {
        if (field.getName() == null || field.getName().startsWith("_")) {
            return null;
        }
        PyExpression assignedValue = field.findAssignedValue();
        if (assignedValue instanceof PyCallExpression) {
            PyCallExpression callExpression = (PyCallExpression) assignedValue;
            if (OdooModelUtils.isFieldDeclarationExpression(callExpression)) {
                Map<String, Object> attributes = new HashMap<>();
                String typeName = Optional.of(callExpression)
                        .map(PyCallExpression::getCallee)
                        .map(NavigationItem::getName).orElse(null);
                if (typeName != null) {
                    if (OdooNames.FIELD_TYPE_MANY2ONE.equals(typeName)
                            || OdooNames.FIELD_TYPE_ONE2MANY.equals(typeName)
                            || OdooNames.FIELD_TYPE_MANY2MANY.equals(typeName)) {
                        String comodelName = getCallArgumentStringValue(callExpression, 0, OdooNames.FIELD_ATTR_COMODEL_NAME);
                        attributes.put(OdooNames.FIELD_ATTR_COMODEL_NAME, comodelName);
                    }
                    String related = getCallArgumentStringValue(callExpression, OdooNames.FIELD_ATTR_RELATED);
                    attributes.put(OdooNames.FIELD_ATTR_RELATED, related);
                    if (OdooNames.FIELD_TYPE_MANY2ONE.equals(typeName)) {
                        boolean delegate = getCallArgumentBooleanValue(callExpression, OdooNames.FIELD_ATTR_DELEGATE, false);
                        attributes.put(OdooNames.FIELD_ATTR_DELEGATE, delegate);
                    }
                    return new OdooFieldInfo(field.getName(), field, typeName, attributes);
                }
            }
        }
        return null;
    }

    @Nullable
    private static String getCallArgumentStringValue(@NotNull PyCallExpression callExpression,
                                                     @NotNull String keyword) {
        PyExpression arg = callExpression.getKeywordArgument(keyword);
        if (arg instanceof PyStringLiteralExpression) {
            return ((PyStringLiteralExpression) arg).getStringValue();
        }
        return null;
    }

    @Nullable
    private static String getCallArgumentStringValue(@NotNull PyCallExpression callExpression,
                                                     int index,
                                                     @NotNull String keyword) {
        PyStringLiteralExpression arg = callExpression.getArgument(index, keyword, PyStringLiteralExpression.class);
        if (arg != null) {
            return arg.getStringValue();
        }
        return null;
    }

    private static boolean getCallArgumentBooleanValue(@NotNull PyCallExpression callExpression,
                                                       @NotNull String keyword,
                                                       boolean defaultValue) {
        PyExpression arg = callExpression.getKeywordArgument(keyword);
        if (arg instanceof PyBoolLiteralExpression) {
            return ((PyBoolLiteralExpression) arg).getValue();
        }
        return defaultValue;
    }

    @Nullable
    public PyType getType(@NotNull TypeEvalContext context) {
        PsiFile file = context.getOrigin();
        if (file == null) {
            return null;
        }
        Project project = file.getProject();
        PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(file);
        switch (getTypeName()) {
            case OdooNames.FIELD_TYPE_MANY2ONE:
            case OdooNames.FIELD_TYPE_ONE2MANY:
            case OdooNames.FIELD_TYPE_MANY2MANY:
                String comodel = getComodel();
                if (comodel != null) {
                    OdooRecordSetType recordSetType = OdooNames.FIELD_TYPE_MANY2ONE.equals(myTypeName)
                            ? OdooRecordSetType.ONE : OdooRecordSetType.MULTI;
                    return new OdooModelClassType(comodel, recordSetType, project);
                }
                return null;
            case OdooNames.FIELD_TYPE_BOOLEAN:
                return builtinCache.getBoolType();
            case OdooNames.FIELD_TYPE_ID:
            case OdooNames.FIELD_TYPE_INTEGER:
                return builtinCache.getIntType();
            case OdooNames.FIELD_TYPE_FLOAT:
            case OdooNames.FIELD_TYPE_MONETARY:
                return builtinCache.getFloatType();
            case OdooNames.FIELD_TYPE_CHAR:
            case OdooNames.FIELD_TYPE_TEXT:
            case OdooNames.FIELD_TYPE_HTML:
            case OdooNames.FIELD_TYPE_SELECTION:
                return builtinCache.getStrType();
            case OdooNames.FIELD_TYPE_DATE:
                return OdooPyUtils.getDateType(file);
            case OdooNames.FIELD_TYPE_DATETIME:
                return OdooPyUtils.getDatetimeType(file);
            case OdooNames.FIELD_TYPE_BINARY:
                return builtinCache.getBytesType(PyPsiFacade.getInstance(project).getLanguageLevel(file));
            default:
                return null;
        }
    }

    @Nullable
    public static PyType getFieldType(@Nullable PsiElement field,
                                      @NotNull TypeEvalContext context) {
        OdooFieldInfo info = getInfo(field);
        if (info != null) {
            return info.getType(context);
        }
        return null;
    }
}
