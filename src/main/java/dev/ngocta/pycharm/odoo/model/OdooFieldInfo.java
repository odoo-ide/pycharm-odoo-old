package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.project.Project;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooTypeUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OdooFieldInfo {
    private final PyTargetExpression myField;
    private final String myTypeName;
    private String myComodel = null;
    private String myRelated = null;
    private static final Set<String> knownFieldTypeNames = new HashSet<>(Arrays.asList(
            OdooNames.FIELD_TYPE_MANY2ONE,
            OdooNames.FIELD_TYPE_ONE2MANY,
            OdooNames.FIELD_TYPE_MANY2MANY,
            OdooNames.FIELD_TYPE_INTEGER,
            OdooNames.FIELD_TYPE_FLOAT,
            OdooNames.FIELD_TYPE_BOOLEAN,
            OdooNames.FIELD_TYPE_INTEGER,
            OdooNames.FIELD_TYPE_FLOAT,
            OdooNames.FIELD_TYPE_MONETARY,
            OdooNames.FIELD_TYPE_CHAR,
            OdooNames.FIELD_TYPE_TEXT,
            OdooNames.FIELD_TYPE_SELECTION,
            OdooNames.FIELD_TYPE_DATE,
            OdooNames.FIELD_TYPE_DATETIME
    ));

    private OdooFieldInfo(@NotNull PyTargetExpression field,
                          @NotNull String typeName) {
        myField = field;
        myTypeName = typeName;
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
            PyExpression callee = callExpression.getCallee();
            if (callee != null && callee.getName() != null) {
                String calleeName = callee.getName();
                if (knownFieldTypeNames.contains(calleeName)) {
                    OdooFieldInfo info = new OdooFieldInfo(field, calleeName);
                    switch (calleeName) {
                        case OdooNames.FIELD_TYPE_MANY2ONE:
                        case OdooNames.FIELD_TYPE_ONE2MANY:
                        case OdooNames.FIELD_TYPE_MANY2MANY:
                            PyStringLiteralExpression comodelExpression = callExpression.getArgument(0, OdooNames.FIELD_PARAM_COMODEL_NAME, PyStringLiteralExpression.class);
                            if (comodelExpression != null) {
                                info.myComodel = comodelExpression.getStringValue();
                            } else {
                                PyExpression relatedExpression = callExpression.getKeywordArgument(OdooNames.FIELD_PARAM_RELATED);
                                if (relatedExpression instanceof PyStringLiteralExpression) {
                                    info.myRelated = ((PyStringLiteralExpression) relatedExpression).getStringValue();
                                }
                            }
                            break;
                    }
                    return info;
                }
            }
        }
        return null;
    }

    @NotNull
    public String getTypeName() {
        return myTypeName;
    }

    public String getComodel() {
        return myComodel;
    }

    public String getRelated() {
        return myRelated;
    }

    @Nullable
    public PyType getType(@NotNull TypeEvalContext context) {
        Project project = myField.getProject();
        PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(myField);
        switch (myTypeName) {
            case OdooNames.FIELD_TYPE_MANY2ONE:
            case OdooNames.FIELD_TYPE_ONE2MANY:
            case OdooNames.FIELD_TYPE_MANY2MANY:
                if (myComodel != null) {
                    OdooRecordSetType recordSetType = OdooNames.FIELD_TYPE_MANY2ONE.equals(myTypeName) ? OdooRecordSetType.ONE : OdooRecordSetType.MULTI;
                    return new OdooModelClassType(myComodel, recordSetType, project);
                } else if (myRelated != null) {
                    OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(myField);
                    if (modelClass != null) {
                        PyTargetExpression relatedField = modelClass.findFieldByPath(myRelated, context);
                        if (relatedField != null) {
                            return getFieldType(relatedField, context);
                        }
                    }
                }
                return null;
            case OdooNames.FIELD_TYPE_BOOLEAN:
                return builtinCache.getBoolType();
            case OdooNames.FIELD_TYPE_INTEGER:
                return builtinCache.getIntType();
            case OdooNames.FIELD_TYPE_FLOAT:
            case OdooNames.FIELD_TYPE_MONETARY:
                return builtinCache.getFloatType();
            case OdooNames.FIELD_TYPE_CHAR:
            case OdooNames.FIELD_TYPE_TEXT:
            case OdooNames.FIELD_TYPE_SELECTION:
                return builtinCache.getStrType();
            case OdooNames.FIELD_TYPE_DATE:
                return OdooTypeUtils.getDateType(myField);
            case OdooNames.FIELD_TYPE_DATETIME:
                return OdooTypeUtils.getDatetimeType(myField);
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
