package dev.ngocta.pycharm.odoo.module;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext;
import com.jetbrains.python.psi.resolve.PyResolveImportUtil;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooAddonsImportResolver implements PyImportResolver {
    @Nullable
    @Override
    public PsiElement resolveImportReference(@NotNull QualifiedName name,
                                             @NotNull PyQualifiedNameResolveContext context,
                                             boolean withRoot) {
        Project project = context.getProject();
        if (DumbService.isDumb(project)) {
            return null;
        }
        List<String> components = name.getComponents();
        if (components.size() < 1 || !OdooNames.ODOO.equals(components.get(0))) {
            return null;
        }
        if (components.size() > 2 && OdooNames.ADDONS.equals(components.get(1))) {
            String moduleName = components.get(2);
            PsiElement foothold = context.getFoothold();
            if (foothold == null) {
                return null;
            }
            OdooModule module = OdooModuleIndex.getModule(moduleName, foothold);
            if (module != null) {
                QualifiedName relatedName = name.subQualifiedName(3, name.getComponentCount());
                List<PsiElement> refs = PyResolveImportUtil.resolveModuleAt(relatedName, module.getDirectory(), context);
                if (!refs.isEmpty()) {
                    return refs.get(0);
                }
            }
        } else {
            context = context.copyWithoutForeign().copyWithoutStubs();
            List<PsiElement> refs = PyResolveImportUtil.resolveQualifiedName(name, context);
            if (!refs.isEmpty()) {
                return refs.get(0);
            }
        }
        return null;
    }
}
