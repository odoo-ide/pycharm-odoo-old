package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
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
        VirtualFile virtualFile;
        if (element instanceof PsiDirectory) {
            virtualFile = ((PsiDirectory) element).getVirtualFile();
        } else {
            PsiFile psiFile = FileContextUtil.getContextFile(element);
            if (psiFile == null) {
                return null;
            }
            psiFile = psiFile.getOriginalFile();
            virtualFile = psiFile.getVirtualFile();
        }
        VirtualFile dir = getContainingOdooModuleDirectory(virtualFile);
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
    public static <T extends PsiElement> List<T> sortElementByOdooModuleDependOrder(@NotNull Collection<T> elements) {
        return sortElementByOdooModuleDependOrder(elements, false);
    }

    @NotNull
    public static <T extends PsiElement> List<T> sortElementByOdooModuleDependOrder(@NotNull Collection<T> elements,
                                                                                    boolean reverse) {
        Map<T, Integer> element2DependsCount = new HashMap<>();
        for (T element : elements) {
            if (element != null) {
                OdooModule module = getContainingOdooModule(element);
                if (module != null) {
                    element2DependsCount.put(element, module.getFlattenedDependsGraph().size());
                }
            }
        }
        List<T> sortedElements = new LinkedList<>(element2DependsCount.keySet());
        sortedElements.sort((e1, e2) -> {
            int count1 = element2DependsCount.get(e1);
            int count2 = element2DependsCount.get(e2);
            return reverse ? count1 - count2 : count2 - count1;
        });
        return sortedElements;
    }
}
