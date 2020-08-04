package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.psi.resolve.JavaScriptResolveScopeProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooJSElementScopeProvider extends JavaScriptResolveScopeProvider {
    @Nullable
    @Override
    public GlobalSearchScope getElementResolveScope(@NotNull PsiElement psiElement) {
        PsiFile file = psiElement.getContainingFile();
        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(file);
        if (odooModule == null) {
            return null;
        }
        GlobalSearchScope modulesScope = odooModule.getOdooModuleWithDependenciesScope();
        GlobalSearchScope librariesScope = getPredefinedLibraryScope(file.getProject());
        return modulesScope.union(librariesScope);
    }

    private GlobalSearchScope getPredefinedLibraryScope(@NotNull Project project) {
        return CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            List<VirtualFile> predefinedLibraryFiles = this.getPredefinedLibraryFiles(project);
            GlobalSearchScope librariesScope = GlobalSearchScope.filesScope(project, predefinedLibraryFiles);
            return CachedValueProvider.Result.create(librariesScope, PsiModificationTracker.MODIFICATION_COUNT);
        });
    }
}
