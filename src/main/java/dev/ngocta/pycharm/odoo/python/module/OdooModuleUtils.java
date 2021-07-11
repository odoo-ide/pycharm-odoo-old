package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModuleUtils {
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
        if (element instanceof PomTargetPsiElement) {
            PomTarget target = ((PomTargetPsiElement) element).getTarget();
            if (target instanceof PsiTarget) {
                return getContainingOdooModule(((PsiTarget) target).getNavigationElement());
            }
        }
        return getContainingOdooModule(element.getContainingFile());
    }

    @Nullable
    public static OdooModule getContainingOdooModule(@Nullable PsiFile file) {
        if (file == null) {
            return null;
        }
        return PyUtil.getNullableParameterizedCachedValue(file, file, f -> {
            f = OdooUtils.getOriginalContextFile(f);
            if (f == null) {
                return null;
            }
            return getContainingOdooModule(f.getVirtualFile(), f.getProject());
        });
    }

    @Nullable
    public static OdooModule getContainingOdooModule(@Nullable VirtualFile file,
                                                     @NotNull Project project) {
        if (file == null) {
            return null;
        }
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
        if (elements.isEmpty()) {
            return Collections.emptyList();
        }
        if (elements.size() == 1) {
            return new LinkedList<>(elements);
        }
        Map<OdooModule, List<T>> module2Elements = new LinkedHashMap<>();
        for (T element : elements) {
            OdooModule module = getContainingOdooModule(element);
            if (module != null) {
                module2Elements.computeIfAbsent(module, k -> new LinkedList<>()).add(element);
            }
        }
        Map<OdooModule, Integer> module2DependsCount = new HashMap<>();
        for (OdooModule module : module2Elements.keySet()) {
            module2DependsCount.put(module, module.getRecursiveDependencies().size());
        }
        List<OdooModule> sortedModules = new LinkedList<>(module2DependsCount.keySet());
        sortedModules.sort((e1, e2) -> {
            int count1 = module2DependsCount.get(e1);
            int count2 = module2DependsCount.get(e2);
            return count2 - count1;
        });
        List<T> sortedElements = new LinkedList<>();
        for (OdooModule module : sortedModules) {
            sortedElements.addAll(module2Elements.get(module));
        }
        if (reverse) {
            Collections.reverse(sortedElements);
        }
        return sortedElements;
    }

    @Nullable
    public static String getLocationStringForFile(@Nullable VirtualFile file) {
        VirtualFile moduleDir = getContainingOdooModuleDirectory(file);
        if (moduleDir == null) {
            return null;
        }
        VirtualFile parent = moduleDir.getParent();
        if (parent == null) {
            return null;
        }
        return VfsUtil.getRelativePath(file, parent);
    }

    @Nullable
    public static String getLocationStringForFile(@Nullable PsiFile file) {
        return file == null ? null : getLocationStringForFile(file.getVirtualFile());
    }

    @NotNull
    public static Collection<String> getSystemWideOdooModuleNames() {
        return Arrays.asList("base", "web");
    }

    @NotNull
    public static Collection<OdooModule> getSystemWideOdooModules(@NotNull PsiElement anchor) {
        List<OdooModule> modules = new LinkedList<>();
        for (String moduleName : getSystemWideOdooModuleNames()) {
            OdooModule module = OdooModuleIndex.getOdooModuleByName(moduleName, anchor);
            if (module != null) {
                modules.add(module);
            }
        }
        return modules;
    }

    @NotNull
    public static GlobalSearchScope getSystemWideOdooModulesScope(@NotNull PsiElement anchor) {
        List<GlobalSearchScope> scopes = new LinkedList<>();
        for (OdooModule module : getSystemWideOdooModules(anchor)) {
            scopes.add(module.getOdooModuleScope());
        }
        if (scopes.isEmpty()) {
            return GlobalSearchScope.EMPTY_SCOPE;
        }
        return GlobalSearchScope.union(scopes);
    }

    @NotNull
    public static GlobalSearchScope getOdooModuleWithDependenciesOrSystemWideModulesScope(@NotNull PsiElement anchor) {
        OdooModule module = getContainingOdooModule(anchor);
        if (module != null) {
            return module.getOdooModuleWithDependenciesScope();
        }
        return getSystemWideOdooModulesScope(anchor);
    }

    @NotNull
    public static String getExternalIdOfModule(@NotNull String module) {
        return "module_" + module;
    }
}
