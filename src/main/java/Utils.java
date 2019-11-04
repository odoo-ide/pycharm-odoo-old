import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.PythonFileType;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

public class Utils {
    @Nullable
    public static VirtualFile getOdooModuleDir(@NotNull VirtualFile file) {
        VirtualFile cur = file;
        while (cur != null && cur.isDirectory()) {
            if (cur.findChild(OdooNames.MANIFEST) != null) {
                return cur;
            }
            cur = cur.getParent();
        }
        return null;
    }

    public static boolean isOdooModelFile(@NotNull VirtualFile file) {
        if (file.isDirectory()) {
            return false;
        }
        if (!file.getFileType().equals(PythonFileType.INSTANCE)) {
            return false;
        }
        return getOdooModuleDir(file) != null;
    }

    public static boolean isOdooModelFile(PsiFile file) {
        return isOdooModelFile(file.getVirtualFile());
    }
}
