import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

public class OdooModuleIndex extends FileBasedIndexExtension<String, OdooModuleInfo> {
    public static final @NotNull ID<String, OdooModuleInfo> NAME = ID.create("odoo.module");

    @Override
    public @NotNull ID<String, OdooModuleInfo> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, OdooModuleInfo, FileContent> getIndexer() {
        return OdooModuleDataIndexer.INSTANCE;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<OdooModuleInfo> getValueExternalizer() {
        return OdooModuleDataExternalizer.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return OdooManifestInputFilter.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
