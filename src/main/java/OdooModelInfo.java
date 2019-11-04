import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class OdooModelInfo {
    private final String myName;
    private final String myModuleName;
    private final boolean myIsPrimary;
    private final List<String> myInherit;

    public OdooModelInfo(@NotNull String name, @NotNull String moduleName, boolean isPrimary, @Nullable List<String> inherit) {
        myName = name;
        myModuleName = moduleName;
        myIsPrimary = isPrimary;
        myInherit = inherit;
    }

    @NotNull
    public String getName() {
        return myName;
    }

    @NotNull
    public String getModuleName() {
        return myModuleName;
    }

    public boolean isPrimary() {
        return myIsPrimary;
    }

    @Nullable
    public List<String> getInherit() {
        return myInherit;
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
        List<String> inherit = null;
        boolean isPrimary = false;

        PyTargetExpression nameExpr = pyClass.findClassAttribute(OdooNames.MODEL_NAME, false, null);
        if (nameExpr != null) {
            PyExpression valueExpr = nameExpr.findAssignedValue();
            if (valueExpr instanceof PyStringLiteralExpression) {
                model = ((PyStringLiteralExpression) valueExpr).getStringValue();
                isPrimary = true;
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
        if (model != null) {
            return new OdooModelInfo(model, moduleName, isPrimary, inherit);
        }
        return null;
    }
}
