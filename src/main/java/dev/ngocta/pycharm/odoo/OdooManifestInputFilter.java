package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

public class OdooManifestInputFilter implements FileBasedIndex.InputFilter {
    public static final FileBasedIndex.InputFilter INSTANCE = new OdooManifestInputFilter();

    @Override
    public boolean acceptInput(@NotNull VirtualFile file) {
        return file.getName().equals(OdooNames.MANIFEST);
    }
}
