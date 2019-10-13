import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileContent;
import com.jetbrains.python.psi.*;

import java.util.HashMap;
import java.util.Map;

public class Utils {
    public static Map<String, Object> parseManifest(FileContent manifest) {
        VirtualFile file = manifest.getFile();
        if (!file.getName().equals(OdooNames.MANIFEST)) {
            return null;
        }
        HashMap<String, Object> data = new HashMap<>();

        PyDictLiteralExpression dictExpression = PsiTreeUtil.findChildOfType(manifest.getPsiFile(), PyDictLiteralExpression.class);
        if (dictExpression == null) {
            return null;
        }
        for (PyKeyValueExpression kvExpression : dictExpression.getElements()) {
            PyExpression key = kvExpression.getKey();
            if (key instanceof PyStringLiteralExpression) {
                String keyName = ((PyStringLiteralExpression) key).getStringValue();
                PyExpression value = kvExpression.getValue();
                if (value instanceof PySequenceExpression) {
                    data.put(keyName, PyUtil.strListValue(value));
                }
            }
        }
        return data;
    }
}
