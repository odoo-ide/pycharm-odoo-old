package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface OdooRecord {
    @NotNull
    String getName();

    @Nullable
    String getModel();

    @NotNull
    String getModule();

    @NotNull
    String getId();

    @Nullable
    OdooRecordSubType getSubType();

    @Nullable
    VirtualFile getDataFile();

    List<PsiElement> getElements(@NotNull Project project);

    List<NavigationItem> getNavigationItems(@NotNull Project project);
}
