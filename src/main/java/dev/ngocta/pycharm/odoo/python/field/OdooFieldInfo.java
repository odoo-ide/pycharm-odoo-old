package dev.ngocta.pycharm.odoo.python.field;

import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import dev.ngocta.pycharm.odoo.python.OdooPyNames;

import java.util.List;

public class OdooFieldInfo {
    private String myType = null;
    private String myComodel = null;
    private String myRelated = null;

    private OdooFieldInfo() {
    }

    @Nullable
    public static OdooFieldInfo readFromClassAttribute(@NotNull PyTargetExpression attribute, @NotNull TypeEvalContext context) {
        return CachedValuesManager.getCachedValue(attribute, () -> {
            OdooFieldInfo info = null;
            PyExpression assignedValue = attribute.findAssignedValue();
            if (assignedValue instanceof PyCallExpression) {
                PyCallExpression callExpression = (PyCallExpression) assignedValue;
                PyExpression callee = callExpression.getCallee();
                if (callee != null && callee.getName() != null) {
                    String calleeName = callee.getName();
                    PyType callType = context.getType(callExpression);
                    if (callType instanceof PyClassType) {
                        List<PyClass> ancestor = ((PyClassType) callType).getPyClass().getAncestorClasses(context);
                        for (PyClass cls : ancestor) {
                            if (OdooPyNames.FIELD_QNAME.equals(cls.getQualifiedName())) {
                                info = new OdooFieldInfo();
                                info.myType = callee.getName();
                                switch (calleeName) {
                                    case OdooPyNames.MANY2ONE:
                                    case OdooPyNames.ONE2MANY:
                                    case OdooPyNames.MANY2MANY:
                                        PyStringLiteralExpression comodelExpression = callExpression.getArgument(0, OdooPyNames.COMODEL_NAME, PyStringLiteralExpression.class);
                                        if (comodelExpression != null) {
                                            info.myComodel = comodelExpression.getStringValue();
                                        }
                                }
                                PyExpression relatedExpression = callExpression.getKeywordArgument(OdooPyNames.RELATED);
                                if (relatedExpression instanceof PyStringLiteralExpression) {
                                    info.myRelated = ((PyStringLiteralExpression) relatedExpression).getStringValue();
                                }
                            }
                        }
                    }
                }
            }
            return CachedValueProvider.Result.create(info, attribute);
        });
    }

    @NotNull
    public String getType() {
        return myType;
    }

    public String getComodel() {
        return myComodel;
    }

    public String getRelated() {
        return myRelated;
    }
}
