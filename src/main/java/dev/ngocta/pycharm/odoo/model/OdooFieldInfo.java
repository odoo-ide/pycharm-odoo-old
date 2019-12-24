package dev.ngocta.pycharm.odoo.model;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooTypeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OdooFieldInfo {
    private PyTargetExpression myField;
    private final String myTypeName;
    private final Map<String, String> myAttributes;

    private OdooFieldInfo(@NotNull PyTargetExpression field,
                          @NotNull String typeName,
                          @NotNull Map<String, String> attributes) {
        myField = field;
        myTypeName = typeName;
        myAttributes = attributes;
    }

    @NotNull
    public String getTypeName() {
        return myTypeName;
    }

    @Nullable
    public String getComodel() {
        return myAttributes.get(OdooNames.FIELD_ATTR_COMODEL_NAME);
    }

    @Nullable
    public String getRelated() {
        return myAttributes.get(OdooNames.FIELD_ATTR_RELATED);
    }

    @Nullable
    public static OdooFieldInfo getInfo(@NotNull PyTargetExpression field) {
        return CachedValuesManager.getCachedValue(field, () -> {
            OdooFieldInfo info = getInfoInner(field);
            return CachedValueProvider.Result.create(info, field);
        });
    }

    @Nullable
    private static OdooFieldInfo getInfoInner(@NotNull PyTargetExpression field) {
        PyExpression assignedValue = field.findAssignedValue();
        if (assignedValue instanceof PyCallExpression) {
            PyCallExpression callExpression = (PyCallExpression) assignedValue;
            if (OdooModelUtils.isFieldCallExpression(callExpression)) {
                Map<String, String> attributes = new HashMap<>();
                String typeName = Optional.of(callExpression)
                        .map(PyCallExpression::getCallee)
                        .map(NavigationItem::getName).orElse(null);
                if (typeName != null) {
                    switch (typeName) {
                        case OdooNames.FIELD_TYPE_MANY2ONE:
                        case OdooNames.FIELD_TYPE_ONE2MANY:
                        case OdooNames.FIELD_TYPE_MANY2MANY:
                            PyStringLiteralExpression comodelExpression = callExpression.getArgument(0, OdooNames.FIELD_ATTR_COMODEL_NAME, PyStringLiteralExpression.class);
                            if (comodelExpression != null) {
                                attributes.put(OdooNames.FIELD_ATTR_COMODEL_NAME, comodelExpression.getStringValue());
                            } else {
                                PyExpression relatedExpression = callExpression.getKeywordArgument(OdooNames.FIELD_ATTR_RELATED);
                                if (relatedExpression instanceof PyStringLiteralExpression) {
                                    attributes.put(OdooNames.FIELD_ATTR_RELATED, ((PyStringLiteralExpression) relatedExpression).getStringValue());
                                }
                            }
                            break;
                    }
                    return new OdooFieldInfo(field, typeName, attributes);
                }
            }
        }
        return null;
    }

    @Nullable
    public PyType getType(@NotNull TypeEvalContext context) {
        Project project = myField.getProject();
        PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(myField);
        switch (getTypeName()) {
            case OdooNames.FIELD_TYPE_MANY2ONE:
            case OdooNames.FIELD_TYPE_ONE2MANY:
            case OdooNames.FIELD_TYPE_MANY2MANY:
                if (getComodel() != null) {
                    OdooRecordSetType recordSetType = OdooNames.FIELD_TYPE_MANY2ONE.equals(myTypeName) ? OdooRecordSetType.ONE : OdooRecordSetType.MULTI;
                    return new OdooModelClassType(getComodel(), recordSetType, project);
                } else if (getRelated() != null) {
                    OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(myField);
                    if (modelClass != null) {
                        PyTargetExpression relatedField = modelClass.findFieldByPath(getRelated(), context);
                        OdooFieldInfo relatedFieldInfo = getInfo(relatedField);
                        if (relatedFieldInfo != null) {
                            return relatedFieldInfo.getType(context);
                        }
                    }
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
                return OdooTypeUtils.getDateType(myField);
            case OdooNames.FIELD_TYPE_DATETIME:
                return OdooTypeUtils.getDatetimeType(myField);
            case OdooNames.FIELD_TYPE_BINARY:
                return builtinCache.getBytesType(PyPsiFacade.getInstance(project).getLanguageLevel(myField));
            default:
                return null;
        }
    }

    @Nullable
    public static PyType getFieldType(@NotNull PyTargetExpression field, @NotNull TypeEvalContext context) {
        OdooFieldInfo info = getInfo(field);
        if (info != null) {
            return info.getType(context);
        }
        return null;
    }
}
