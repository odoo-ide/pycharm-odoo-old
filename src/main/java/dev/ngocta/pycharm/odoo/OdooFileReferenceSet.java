package dev.ngocta.pycharm.odoo;

import com.intellij.ide.BrowserUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import dev.ngocta.pycharm.odoo.module.OdooModuleIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OdooFileReferenceSet extends FileReferenceSet {
    public OdooFileReferenceSet(@NotNull PsiElement element) {
        super(element);
    }

    @NotNull
    @Override
    public Collection<PsiFileSystemItem> computeDefaultContexts() {
        final PsiFile file = getContainingFile();
        if (file == null) {
            return Collections.emptyList();
        }

        if (isAbsolutePathReference()) {
            return PyUtil.getParameterizedCachedValue(file, null, param -> {
                return OdooModuleIndex.getAllModules(getElement().getProject()).stream()
                        .map(OdooModule::getDirectory)
                        .map(PsiDirectory::getParent)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
            });
        }

        if (file.getParent() != null) {
            return Collections.singletonList(file.getParent());
        }

        return Collections.emptyList();
    }

    @Override
    protected List<FileReference> reparse(String str, int startInElement) {
        if (BrowserUtil.isAbsoluteURL(str.trim())) {
            return Collections.emptyList();
        }
        return super.reparse(str, startInElement);
    }
}
