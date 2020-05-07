package dev.ngocta.pycharm.odoo.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.jetbrains.python.PyNames;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModuleUtils {
    private OdooModuleUtils() {
    }

    public static boolean isInOdooModule(@Nullable PsiElement element) {
        return getContainingOdooModulePsiDirectory(element) != null;
    }

    public static boolean isInOdooModule(@Nullable VirtualFile file) {
        return getContainingOdooModuleDirectory(file) != null;
    }

    @Nullable
    public static PsiDirectory getContainingOdooModulePsiDirectory(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        VirtualFile dir;
        if (element instanceof PsiDirectory) {
            dir = getContainingOdooModuleDirectory(((PsiDirectory) element).getVirtualFile());
        } else {
            PsiFile file = element.getContainingFile();
            if (file == null) {
                return null;
            }
            PsiElement context = file.getContext();
            if (context != null && !(context instanceof PsiDirectory)) {
                file = context.getContainingFile();
                if (file == null) {
                    return null;
                }
            }
            file = file.getOriginalFile();
            dir = getContainingOdooModuleDirectory(file.getVirtualFile());
        }
        if (dir != null) {
            return PsiManager.getInstance(element.getProject()).findDirectory(dir);
        }
        return null;
    }

    @Nullable
    public static VirtualFile getContainingOdooModuleDirectory(@Nullable VirtualFile file) {
        while (file != null) {
            if (isOdooModuleDirectory(file)) {
                return file;
            }
            file = file.getParent();
        }
        return null;
    }

    public static boolean isOdooModuleDirectory(@Nullable VirtualFile dir) {
        return dir != null && dir.isDirectory()
                && dir.findChild(OdooNames.MANIFEST_FILE_NAME) != null
                && dir.findChild(PyNames.INIT_DOT_PY) != null;
    }

    @Nullable
    public static OdooModule getContainingOdooModule(@Nullable PsiElement element) {
        PsiDirectory directory = getContainingOdooModulePsiDirectory(element);
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
