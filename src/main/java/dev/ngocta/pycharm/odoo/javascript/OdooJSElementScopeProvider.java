package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.library.JSLibraryMappings;
import com.intellij.lang.javascript.psi.resolve.JavaScriptResolveScopeProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class OdooJSElementScopeProvider extends JavaScriptResolveScopeProvider {
    @Override
    public GlobalSearchScope getResolveScope(@NotNull VirtualFile file, Project project) {
        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(file, project);
        if (odooModule == null) {
            return super.getResolveScope(file, project);
        }
        GlobalSearchScope modulesScope = odooModule.getOdooModuleWithDependenciesScope();
        List<VirtualFile> predefinedLibraryFiles = this.getPredefinedLibraryFiles(project);
        GlobalSearchScope predefinedLibraryScope = GlobalSearchScope.filesScope(project, predefinedLibraryFiles);
        GlobalSearchScope libraryScope = JSLibraryMappings.getInstance(project).getLibraryScopeForFileWithoutPredefined(
                file, Collections.emptySet(), Collections.emptySet());
        return modulesScope.union(predefinedLibraryScope).union(libraryScope);
    }
}
