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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
        PsiFile file = element.getContainingFile();
        if (file == null) {
            return null;
        }
        PsiElement context = file.getContext();
        if (context != null && !(context instanceof PsiDirectory)) {
            file = file.getContext().getContainingFile();
        }
        file = file.getOriginalFile();
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

    @NotNull
    public static <T extends PsiElement> List<T> sortElementByOdooModuleOrder(@NotNull Collection<T> elements) {
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }
        List<T> sortedElements = new LinkedList<T>(elements);
        sortedElements.sort((e1, e2) -> {
            OdooModule m1 = getContainingOdooModule(e1);
            OdooModule m2 = getContainingOdooModule(e2);
            if (m1 == null || m2 == null || m1 == m2) {
                return 0;
            }
            return m1.isDependOn(m2) ? -1 : 1;
        });
        return sortedElements;
    }
}
