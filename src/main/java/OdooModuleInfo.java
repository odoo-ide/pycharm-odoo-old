import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileContent;
import com.jetbrains.python.psi.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OdooModuleInfo {
    private String myName;
    private String myPath;
    private List<String> myDepends;

    public OdooModuleInfo(String name, String path, List<String> depends) {
        myName = name;
        myPath = path;
        myDepends = depends;
    }

    public String getName() {
        return myName;
    }

    public String getPath() {
        return myPath;
    }

    public List<String> getDepends() {
        return myDepends;
    }

    public static OdooModuleInfo readFromManifest(FileContent manifest) {
        VirtualFile file = manifest.getFile();
        if (!file.getName().equals(OdooNames.MANIFEST)) {
            return null;
        }
        VirtualFile dir = file.getParent();
        String name = dir.getName();
        String path = dir.getPath();
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
        return new OdooModuleInfo(name, path, depends);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof OdooModuleInfo)) {
            return false;
        }

        OdooModuleInfo info = (OdooModuleInfo) obj;
        return Arrays.equals(info.getFields(), getFields());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getFields());
    }

    private Object[] getFields() {
        return new Object[]{myName, myPath, myDepends};
    }
}
