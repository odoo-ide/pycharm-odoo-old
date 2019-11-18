package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileContent;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class OdooModuleInfo {
    private List<String> myDepends;

    private OdooModuleInfo(List<String> depends) {
        myDepends = depends;
    }

    @NotNull
    public List<String> getDepends() {
        return myDepends;
    }

    @Nullable
    public static OdooModuleInfo readFromManifest(@NotNull FileContent manifest) {
        VirtualFile file = manifest.getFile();
        if (!file.getName().equals(OdooNames.MANIFEST)) {
            return null;
        }

        PyDictLiteralExpression dictExpression = PsiTreeUtil.findChildOfType(manifest.getPsiFile(), PyDictLiteralExpression.class);
        if (dictExpression == null) {
            return null;
        }

        List<String> depends = null;
        for (PyKeyValueExpression kvExpression : dictExpression.getElements()) {
            PyExpression key = kvExpression.getKey();
            if (key instanceof PyStringLiteralExpression) {
                String keyName = ((PyStringLiteralExpression) key).getStringValue();
                PyExpression value = kvExpression.getValue();
                if (keyName.equals("depends") && value instanceof PySequenceExpression) {
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
