package dev.ngocta.pycharm.odoo.xml;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface OdooRecordDefinition extends NavigationItem {
    @NotNull
    String getId();

    @NotNull
    String getModel();

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
        return new OdooRecordPresentation(this);
    }
}
