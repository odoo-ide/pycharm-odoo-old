package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OdooNavigableRecord extends OdooRecord, NavigationItem {
    @Nullable
    VirtualFile getFile();

    @NotNull
    PsiElement getNavigationElement();

    @Nullable
    @Override
    default String getName() {
        return getId();
    }

    @Nullable
    @Override
    default ItemPresentation getPresentation() {
        return new OdooNavigableRecordPresentation(this);
    }
}
