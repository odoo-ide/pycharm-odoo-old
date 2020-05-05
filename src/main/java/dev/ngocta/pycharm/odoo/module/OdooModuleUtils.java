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

import java.util.*;

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
        Map<T, Integer> element2DependsCount = new HashMap<>();
        List<T> notInModuleElements = new LinkedList<>();
        for (T element : elements) {
            if (element != null) {
                OdooModule module = getContainingOdooModule(element);
                if (module != null) {
                    element2DependsCount.put(element, module.getFlattenedDependsGraph().size());
                } else {
                    notInModuleElements.add(element);
                }
            }
        }
        List<T> sortedElements = new LinkedList<>(element2DependsCount.keySet());
        sortedElements.sort((e1, e2) -> {
            int count1 = element2DependsCount.get(e1);
            int count2 = element2DependsCount.get(e2);
            return count2 - count1;
        });
        sortedElements.addAll(notInModuleElements);
        return sortedElements;
    }
}
