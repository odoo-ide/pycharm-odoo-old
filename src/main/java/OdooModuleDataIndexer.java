import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.DataIndexer;
import com.intellij.util.indexing.FileContent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class OdooModuleDataIndexer implements DataIndexer<String, OdooModuleInfo, FileContent> {
    public static final OdooModuleDataIndexer INSTANCE = new OdooModuleDataIndexer();

    @Override
    public @NotNull Map<String, OdooModuleInfo> map(@NotNull FileContent inputData) {
        HashMap<String, OdooModuleInfo> result = new HashMap<>();
        VirtualFile moduleDir = inputData.getFile().getParent();
        OdooModuleInfo info = OdooModuleInfo.readFromManifest(inputData);
        if (info != null) {
            result.put(moduleDir.getName(), info);
        }
        return result;
    }
}
