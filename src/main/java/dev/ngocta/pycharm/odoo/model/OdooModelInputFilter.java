package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Consumer;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.python.PythonFileType;
import dev.ngocta.pycharm.odoo.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

public class OdooModelInputFilter implements FileBasedIndex.FileTypeSpecificInputFilter {
    @Override
    public void registerFileTypesUsedForIndexing(@NotNull Consumer<FileType> fileTypeSink) {
        fileTypeSink.consume(PythonFileType.INSTANCE);
    }

    @Override
    public boolean acceptInput(@NotNull VirtualFile file) {
        return OdooModuleUtils.isInOdooModule(file);
    }
}
