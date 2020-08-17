package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.PyNames;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModuleUtils {
    public static final PatternCondition<PsiElement> IN_ODOO_MODULE_PATTERN_CONDITION =
            new PatternCondition<PsiElement>("inOdooModule") {
                @Override
                public boolean accepts(@NotNull PsiElement element,
                                       ProcessingContext context) {
                    return isInOdooModule(element);
                }
            };

    private OdooModuleUtils() {
    }

    public static boolean isInOdooModule(@Nullable PsiElement element) {
        return getContainingOdooModule(element) != null;
    }

    public static boolean isInOdooModule(@Nullable VirtualFile file) {
        return getContainingOdooModuleDirectory(file) != null;
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
        if (element == null) {
            return null;
        }
        if (element instanceof PsiFile) {
            return getContainingOdooModule((PsiFile) element);
        }
        if (element instanceof PsiDirectory) {
            return getContainingOdooModule(((PsiDirectory) element).getVirtualFile(), element.getProject());
        }
        return getContainingOdooModule(element.getContainingFile());
    }

    @Nullable
    public static OdooModule getContainingOdooModule(@Nullable PsiFile file) {
        if (file == null) {
            return null;
        }
        file = FileContextUtil.getContextFile(file);
        if (file == null) {
            return null;
        }
        file = file.getOriginalFile();
        return getContainingOdooModule(file.getVirtualFile(), file.getProject());
    }

    @Nullable
    public static OdooModule getContainingOdooModule(@NotNull VirtualFile file,
                                                     @NotNull Project project) {
        VirtualFile dir = getContainingOdooModuleDirectory(file);
        if (dir == null) {
            return null;
        }
        PsiDirectory psiDir = PsiManager.getInstance(project).findDirectory(dir);
        if (psiDir != null) {
            return new OdooModule(psiDir);
        }
        return null;
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
