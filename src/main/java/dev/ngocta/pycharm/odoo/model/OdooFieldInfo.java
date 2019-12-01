package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import dev.ngocta.pycharm.odoo.OdooNames;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class OdooFieldInfo {
    private String myClassName = null;
    private String myComodel = null;
    private String myRelated = null;
    private static final Set<String> knownFieldClassNames = new HashSet<>(Arrays.asList(
            OdooNames.MANY2ONE,
            OdooNames.ONE2MANY,
            OdooNames.MANY2MANY,
            OdooNames.INTEGER,
            OdooNames.FLOAT,
            OdooNames.BOOLEAN,
            OdooNames.INTEGER,
            OdooNames.FLOAT,
            OdooNames.MONETARY,
            OdooNames.CHAR,
            OdooNames.TEXT,
            OdooNames.SELECTION,
            OdooNames.DATE,
            OdooNames.DATETIME
    ));

    private OdooFieldInfo() {
    }

    @Nullable
    public static OdooFieldInfo get(@NotNull PyTargetExpression field, @NotNull TypeEvalContext context) {
        return CachedValuesManager.getCachedValue(field, () -> {
            OdooFieldInfo info = null;
            PyExpression assignedValue = field.findAssignedValue();
            if (assignedValue instanceof PyCallExpression) {
                PyCallExpression callExpression = (PyCallExpression) assignedValue;
                PyExpression callee = callExpression.getCallee();
                if (callee != null && callee.getName() != null) {
                    String calleeName = callee.getName();
                    if (knownFieldClassNames.contains(calleeName)) {
                        info = new OdooFieldInfo();
                        info.myClassName = callee.getName();
                        switch (calleeName) {
                            case OdooNames.MANY2ONE:
                            case OdooNames.ONE2MANY:
                            case OdooNames.MANY2MANY:
                                PyStringLiteralExpression comodelExpression = callExpression.getArgument(0, OdooNames.FIELD_COMODEL_NAME, PyStringLiteralExpression.class);
                                if (comodelExpression != null) {
                                    info.myComodel = comodelExpression.getStringValue();
                                } else {
                                    PyExpression relatedExpression = callExpression.getKeywordArgument(OdooNames.FIELD_RELATED);
                                    if (relatedExpression instanceof PyStringLiteralExpression) {
                                        info.myRelated = ((PyStringLiteralExpression) relatedExpression).getStringValue();
                                    }
                                }
                                break;
                        }
                    }
                }
            }
            return CachedValueProvider.Result.create(info, field);
        });
    }

    @NotNull
    public String getClassName() {
        return myClassName;
    }

    public String getComodel() {
        return myComodel;
    }

    public String getRelated() {
        return myRelated;
    }
}
