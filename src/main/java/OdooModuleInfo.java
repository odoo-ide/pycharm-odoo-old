import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.containers.UnmodifiableTHashMap;
import com.intellij.util.indexing.FileContent;
import com.jetbrains.python.psi.*;
import org.apache.commons.collections.map.UnmodifiableMap;

import java.util.*;

public class OdooModuleInfo {
    private HashMap<String, Object> myInfo = new HashMap<>();
    private final String KEY_DEPENDS = "depends";

    public OdooModuleInfo(List<String> depends) {
        myInfo.put(KEY_DEPENDS, depends);
    }

    @SuppressWarnings("unchecked")
    public List<String> getDepends() {
        return (List<String>) myInfo.get(KEY_DEPENDS);
    }

    public static OdooModuleInfo readFromManifest(FileContent manifest) {
        VirtualFile file = manifest.getFile();
        if (!file.getName().equals(OdooNames.MANIFEST)) {
            return null;
        }
        List<String> depends = Collections.emptyList();

        PyDictLiteralExpression dictExpression = PsiTreeUtil.findChildOfType(manifest.getPsiFile(), PyDictLiteralExpression.class);
        if (dictExpression == null) {
            return null;
        }
        for (PyKeyValueExpression kvExpression : dictExpression.getElements()) {
            PyExpression key = kvExpression.getKey();
            if (key instanceof PyStringLiteralExpression) {
                String keyName = ((PyStringLiteralExpression) key).getStringValue();
                if (keyName.equals("depends")) {
                    PyExpression value = kvExpression.getValue();
                    if (!(value instanceof PySequenceExpression)) {
                        break;
                    }
                    depends = PyExpressionUtils.getPySequenceString((PySequenceExpression) value);
                    break;
                }
            }
        }
        return new OdooModuleInfo(depends);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof OdooModuleInfo)) {
            return false;
        }
        OdooModuleInfo that = (OdooModuleInfo) obj;
        return myInfo.equals(that.myInfo);
    }

    @Override
    public int hashCode() {
        return myInfo.hashCode();
    }
}
