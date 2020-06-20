package dev.ngocta.pycharm.odoo.module;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooManifestInfo {
    private final String myName;
    private final String mySummary;
    private final String[] myDepends;

    private OdooManifestInfo(@Nullable String name,
                             @Nullable String summary,
                             @Nullable String[] depends) {
        myName = name;
        mySummary = summary;
        myDepends = depends;
    }

    @Nullable
    public String getName() {
        return myName;
    }

    @Nullable
    public String getSummary() {
        return mySummary;
    }

    @Nullable
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

        String name = null;
        String summary = null;
        List<String> depends = null;
        for (PyKeyValueExpression kvExpression : dictExpression.getElements()) {
            PyExpression key = kvExpression.getKey();
            if (key instanceof PyStringLiteralExpression) {
                String keyName = ((PyStringLiteralExpression) key).getStringValue();
                PyExpression value = kvExpression.getValue();
                if ("name".equals(keyName) && value instanceof PyStringLiteralExpression) {
                    name = ((PyStringLiteralExpression) value).getStringValue();
                } else if ("summary".equals(keyName) && (value instanceof PyStringLiteralExpression)) {
                    summary = ((PyStringLiteralExpression) value).getStringValue().trim();
                } else if (OdooNames.MANIFEST_DEPENDS.equals(keyName) && value instanceof PySequenceExpression) {
                    depends = PyUtil.strListValue(value);
                }
            }
        }

        return new OdooManifestInfo(
                name,
                summary,
                depends != null ? depends.toArray(new String[0]) : null);
    }
}
