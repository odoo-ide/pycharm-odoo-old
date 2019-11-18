package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.indexing.FileBasedIndex;
import org.jetbrains.annotations.NotNull;

public class OdooModelInputFilter implements FileBasedIndex.InputFilter {
    public static final FileBasedIndex.InputFilter INSTANCE = new OdooModelInputFilter();

    @Override
    public boolean acceptInput(@NotNull VirtualFile file) {
        return OdooUtils.isOdooModelFile(file);
    }
}
