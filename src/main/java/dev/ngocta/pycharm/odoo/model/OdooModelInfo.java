package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.IndexNotReadyException;
import com.intellij.openapi.project.Project;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.ArrayUtil;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModelInfo {
    private final String myName;
    private final List<String> myInherit;
    private final Map<String, String> myInherits;
    private final static String[] KNOWN_SUPER_CLASSES = new String[]{
            "models.Model",
            "models.TransientModel",
            "models.AbstractModel"
    };

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
            boolean isModelClass = Arrays.stream(pyClass.getSuperClassExpressions()).anyMatch(pyExpression -> {
                return ArrayUtil.contains(pyExpression.getText(), KNOWN_SUPER_CLASSES);
            });
            Project project = pyClass.getProject();
            if (!isModelClass && !DumbService.isDumb(project)) {
                try {
                    PyClass baseModelClass = OdooModelUtils.getBaseModelClass(pyClass);
                    TypeEvalContext context = TypeEvalContext.codeAnalysis(project, pyClass.getContainingFile());
                    isModelClass = baseModelClass != null && pyClass.isSubclass(baseModelClass, context);
                } catch (IndexNotReadyException e) {
                    return null;
                }
            }
            if (isModelClass) {
                OdooModelInfo info = getInfoInner(pyClass);
                return CachedValueProvider.Result.create(info, pyClass);
            }
            return null;
        });
    }

    @Nullable
    private static OdooModelInfo getInfoInner(@NotNull PyClass pyClass) {
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
            } else {
                inherit = PyUtil.strListValue(valueExpr);
            }
            if (model == null && inherit != null && inherit.size() == 1) {
                model = inherit.get(0);
            }
        }
        if (model == null) {
            return null;
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
        pyClass.visitClassAttributes(attr -> {
            String attrName = attr.getName();
            if (attrName != null) {
                OdooFieldInfo info = OdooFieldInfo.getInfo(attr);
                if (info != null && info.isDelegate() && info.getComodel() != null) {
                    inherits.put(info.getComodel(), attrName);
                }
            }
            return true;
        }, false, null);

        return new OdooModelInfo(model, inherit, inherits);
    }
}
