package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModelInfo {
    private final String myName;
    private final List<String> myInherit;
    private final Map<String, String> myInherits;

    private OdooModelInfo(@NotNull String name,
                          @Nullable List<String> inherit,
                          @Nullable Map<String, String> inherits) {
        myName = name;
        if (inherit == null) {
            inherit = Collections.emptyList();
        }
        myInherit = inherit;
        if (inherits == null) {
            inherits = Collections.emptyMap();
        }
        myInherits = inherits;
    }

    @NotNull
    public String getName() {
        return myName;
    }

    @NotNull
    public List<String> getInherit() {
        return myInherit;
    }

    @NotNull
    public Map<String, String> getInherits() {
        return myInherits;
    }

    public boolean isOriginal() {
        return !myInherit.contains(myName) && !myInherits.containsKey(myName);
    }

    @Nullable
    public static OdooModelInfo getInfo(@NotNull PyClass pyClass) {
        return CachedValuesManager.getCachedValue(pyClass, () -> {
            OdooModelInfo info = getInfoInner(pyClass);
            return CachedValueProvider.Result.createSingleDependency(info, pyClass);
        });
    }

    @Nullable
    private static OdooModelInfo getInfoInner(@NotNull PyClass pyClass) {
        PsiFile psiFile = pyClass.getContainingFile();
        if (psiFile == null) {
            return null;
        }

        PsiDirectory module = OdooUtils.getOdooModule(psiFile);
        if (module == null) {
            return null;
        }
        String model = null;
        List<String> inherit = new LinkedList<>();
        Map<String, String> inherits = new HashMap<>();

        PyTargetExpression nameExpr = pyClass.findClassAttribute(OdooNames.MODEL_NAME, false, null);
        if (nameExpr != null) {
            PyExpression valueExpr = nameExpr.findAssignedValue();
            if (valueExpr instanceof PyStringLiteralExpression) {
                model = ((PyStringLiteralExpression) valueExpr).getStringValue();
                if (model.isEmpty()) {
                    return null;
                }
            }
        }
        PyTargetExpression inheritExpr = pyClass.findClassAttribute(OdooNames.MODEL_INHERIT, false, null);
        if (inheritExpr != null) {
            PyExpression valueExpr = inheritExpr.findAssignedValue();
            if (valueExpr instanceof PyStringLiteralExpression) {
                String inheritModel = ((PyStringLiteralExpression) valueExpr).getStringValue();
                inherit = Collections.singletonList(inheritModel);
                if (model == null) {
                    model = inheritModel;
                }
            } else {
                inherit = PyUtil.strListValue(valueExpr);
            }
        }
        PyTargetExpression inheritsExpr = pyClass.findClassAttribute(OdooNames.MODEL_INHERITS, false, null);
        if (inheritsExpr != null) {
            PyExpression valueExpr = inheritsExpr.findAssignedValue();
            if (valueExpr instanceof PyDictLiteralExpression) {
                Map<String, PyExpression> value = PyUtil.dictValue((PyDictLiteralExpression) valueExpr);
                value.forEach((s, pyExpression) -> {
                    if (pyExpression instanceof PyStringLiteralExpression) {
                        String fieldName = ((PyStringLiteralExpression) pyExpression).getStringValue();
                        inherits.put(s, fieldName);
                    }
                });
            }
        }

        if (model != null) {
            return new OdooModelInfo(model, inherit, inherits);
        }
        return null;
    }
}
