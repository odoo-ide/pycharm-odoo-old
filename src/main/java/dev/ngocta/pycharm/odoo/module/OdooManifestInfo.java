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
    private final String[] myDepends;

    private OdooManifestInfo(String[] depends) {
        myDepends = depends;
    }

    @NotNull
    public String[] getDepends() {
        return myDepends;
    }

    @Nullable
    public static OdooManifestInfo parseManifest(@NotNull PsiFile manifest) {
        return CachedValuesManager.getCachedValue(manifest, () -> {
            OdooManifestInfo info = doParseManifest(manifest);
            return CachedValueProvider.Result.create(info, manifest);
        });
    }

    @Nullable
    private static OdooManifestInfo doParseManifest(@NotNull PsiFile manifest) {
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

        return new OdooManifestInfo(depends.toArray(new String[0]));
    }
}
