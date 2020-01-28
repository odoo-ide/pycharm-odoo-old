package dev.ngocta.pycharm.odoo.module;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class OdooManifestInfo {
    private List<String> myDepends;

    private OdooManifestInfo(List<String> depends) {
        myDepends = depends;
    }

    @NotNull
    public List<String> getDepends() {
        return myDepends;
    }

    @Nullable
    public static OdooManifestInfo getInfo(@NotNull PsiFile manifest) {
        return CachedValuesManager.getCachedValue(manifest, () -> {
            OdooManifestInfo info = getInfoInner(manifest);
            return CachedValueProvider.Result.createSingleDependency(info, manifest.getVirtualFile());
        });
    }

    @Nullable
    private static OdooManifestInfo getInfoInner(@NotNull PsiFile manifest) {
        if (!(manifest instanceof PyFile) || !manifest.getName().equals(OdooNames.MANIFEST_FILE_NAME)) {
            return null;
        }

        PyDictLiteralExpression dictExpression = PsiTreeUtil.findChildOfType(manifest, PyDictLiteralExpression.class);
        if (dictExpression == null) {
            return null;
        }

        List<String> depends = null;
        for (PyKeyValueExpression kvExpression : dictExpression.getElements()) {
            PyExpression key = kvExpression.getKey();
            if (key instanceof PyStringLiteralExpression) {
                String keyName = ((PyStringLiteralExpression) key).getStringValue();
                PyExpression value = kvExpression.getValue();
                if (keyName.equals(OdooNames.MANIFEST_DEPENDS) && value instanceof PySequenceExpression) {
                    depends = PyUtil.strListValue(value);
                    break;
                }
            }
        }
        if (depends == null) {
            depends = Collections.emptyList();
        }

        return new OdooManifestInfo(depends);
    }
}
