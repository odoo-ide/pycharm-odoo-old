package dev.ngocta.pycharm.odoo.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModuleUtils {
    private OdooModuleUtils() {
    }

    public static boolean isInOdooModule(@Nullable PsiElement element) {
        return getContainingOdooModuleDirectory(element) != null;
    }

    @Nullable
    public static PsiDirectory getContainingOdooModuleDirectory(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        PsiFile file = element.getContainingFile().getOriginalFile();
        PsiDirectory directory = file.getParent();
        while (directory != null) {
            if (directory.findFile(OdooNames.MANIFEST_FILE_NAME) != null) {
                return directory;
            }
            directory = directory.getParent();
        }
        return null;
    }

    @Nullable
    public static OdooModule getContainingOdooModule(@Nullable PsiElement element) {
        PsiDirectory directory = getContainingOdooModuleDirectory(element);
        return directory != null ? new OdooModule(directory) : null;
    }

    @Nullable
    public static OdooModule getContainingOdooModule(@NotNull VirtualFile file,
                                                     @NotNull Project project) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
        return getContainingOdooModule(psiFile);
    }
}
