package dev.ngocta.pycharm.odoo;

import com.intellij.ide.BrowserUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.FileReferenceSet;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OdooFileReferenceSet extends FileReferenceSet {
    private final boolean myStartWithOdooModule;

    public OdooFileReferenceSet(@NotNull PsiElement element) {
        this(element, false);
    }

    public OdooFileReferenceSet(@NotNull PsiElement element,
                                boolean startWithOdooModule) {
        super(element);
        myStartWithOdooModule = startWithOdooModule;
    }

    @NotNull
    @Override
    public Collection<PsiFileSystemItem> computeDefaultContexts() {
        final PsiFile file = getContainingFile();
        if (file == null) {
            return Collections.emptyList();
        }

        if (isAbsolutePathReference() || myStartWithOdooModule) {
            return PyUtil.getParameterizedCachedValue(file, null, param -> {
                return OdooModuleIndex.getAvailableOdooModules(getElement()).stream()
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
    protected List<FileReference> reparse(String str,
                                          int startInElement) {
        if (BrowserUtil.isAbsoluteURL(str.trim())) {
            return Collections.emptyList();
        }
        return super.reparse(str, startInElement);
    }
}
