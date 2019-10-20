import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.python.PythonFileType;
import org.jetbrains.annotations.NotNull;

public class OdooModelInputFilter implements FileBasedIndex.InputFilter {
    public static final FileBasedIndex.InputFilter INSTANCE = new OdooModelInputFilter();

    @Override
    public boolean acceptInput(@NotNull VirtualFile file) {
        VirtualFile parent = file.getParent();
        return parent.isDirectory() && parent.getName().equals("models") && file.getFileType().equals(PythonFileType.INSTANCE);
    }
}
