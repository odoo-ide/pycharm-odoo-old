package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class OdooModuleInfo {
    private List<String> myDepends;
    private static final Key<CachedValue<OdooModuleInfo>> KEY = new Key<>("OdooModuleInfo");

    private OdooModuleInfo(List<String> depends) {
        myDepends = depends;
    }

    @NotNull
    public List<String> getDepends() {
        return myDepends;
    }

    @Nullable
    public static OdooModuleInfo readFromManifest(@NotNull PsiFile file) {
        return CachedValuesManager.getCachedValue(file, KEY, () -> {
            OdooModuleInfo info = doReadFromManifest(file);
            return CachedValueProvider.Result.createSingleDependency(info, file);
        });
    }

    @Nullable
    private static OdooModuleInfo doReadFromManifest(@NotNull PsiFile file) {
        if (!(file instanceof PyFile) || !file.getName().equals(OdooNames.MANIFEST)) {
            return null;
        }

        PyDictLiteralExpression dictExpression = PsiTreeUtil.findChildOfType(file, PyDictLiteralExpression.class);
        if (dictExpression == null) {
            return null;
        }

        List<String> depends = null;
        for (PyKeyValueExpression kvExpression : dictExpression.getElements()) {
            PyExpression key = kvExpression.getKey();
            if (key instanceof PyStringLiteralExpression) {
                String keyName = ((PyStringLiteralExpression) key).getStringValue();
                PyExpression value = kvExpression.getValue();
                if (keyName.equals(OdooNames.DEPENDS) && value instanceof PySequenceExpression) {
                    depends = PyUtil.strListValue(value);
                    break;
                }
            }
        }
        if (depends == null) {
            depends = Collections.emptyList();
        }

        return new OdooModuleInfo(depends);
    }
}
