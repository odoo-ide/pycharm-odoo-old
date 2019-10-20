import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class OdooModelInfo {
    private final String myName;
    private final boolean myPrimary;
    private final List<String> myInherit;

    public OdooModelInfo(@NotNull String name, boolean primary, @Nullable List<String> inherit) {
        myName = name;
        myPrimary = primary;
        myInherit = inherit;
    }

    public @NotNull String getName() {
        return myName;
    }

    public boolean isPrimary() {
        return myPrimary;
    }

    public @Nullable List<String> getInherit() {
        return myInherit;
    }

    public static @Nullable OdooModelInfo readFromClass(PyClass pyClass) {
        String model = null;
        List<String> inherit = null;
        boolean primary = false;
        PyTargetExpression nameExpr = pyClass.findClassAttribute(OdooNames.MODEL_NAME, false, null);
        if (nameExpr != null) {
            PyExpression valueExpr = nameExpr.findAssignedValue();
            if (valueExpr instanceof PyStringLiteralExpression) {
                model = ((PyStringLiteralExpression) valueExpr).getStringValue();
                primary = true;
            }
        }
        PyTargetExpression inheritExpr = pyClass.findClassAttribute(OdooNames.MODEL_INHERIT, false, null);
        if (inheritExpr != null) {
            PyExpression valueExpr = inheritExpr.findAssignedValue();
            if (valueExpr instanceof PyStringLiteralExpression) {
                String inheritModel = ((PyStringLiteralExpression) valueExpr).getStringValue();
                if (model == null) {
                    model = inheritModel;
                } else {
                    inherit = Collections.singletonList(inheritModel);
                }
            } else {
                inherit = PyUtil.strListValue(valueExpr);
            }
        }
        if (model != null) {
            return new OdooModelInfo(model, primary, inherit);
        }
        return null;
    }
}
