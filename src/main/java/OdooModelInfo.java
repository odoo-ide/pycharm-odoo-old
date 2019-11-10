import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModelInfo {
    private final String myName;
    private final String myModuleName;
    private final List<String> myInherit;
    private final Map<String, String> myInherits;

    private OdooModelInfo(@NotNull String name,
                          @NotNull String moduleName,
                          @Nullable List<String> inherit,
                          @Nullable Map<String, String> inherits) {
        myName = name;
        myModuleName = moduleName;
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
    public String getModuleName() {
        return myModuleName;
    }

    @NotNull
    public List<String> getInherit() {
        return myInherit;
    }

    @NotNull
    public Map<String, String> getInherits() {
        return myInherits;
    }

    @Nullable
    public static OdooModelInfo readFromClass(PyClass pyClass) {
        PsiFile psiFile = pyClass.getContainingFile();
        if (psiFile == null) {
            return null;
        }

        VirtualFile moduleDir = Utils.getOdooModuleDir(psiFile.getVirtualFile());
        if (moduleDir == null) {
            return null;
        }

        String moduleName = moduleDir.getName();
        String model = null;
        List<String> inherit = new LinkedList<>();
        Map<String, String> inherits = new HashMap<>();

        PyTargetExpression nameExpr = pyClass.findClassAttribute(OdooNames.MODEL_NAME, false, null);
        if (nameExpr != null) {
            PyExpression valueExpr = nameExpr.findAssignedValue();
            if (valueExpr instanceof PyStringLiteralExpression) {
                model = ((PyStringLiteralExpression) valueExpr).getStringValue();
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
            return new OdooModelInfo(model, moduleName, inherit, inherits);
        }
        return null;
    }
}
