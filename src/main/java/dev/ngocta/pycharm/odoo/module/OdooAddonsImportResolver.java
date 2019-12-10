package dev.ngocta.pycharm.odoo.module;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext;
import com.jetbrains.python.psi.resolve.PyResolveImportUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooAddonsImportResolver implements PyImportResolver {
    @Nullable
    @Override
    public PsiElement resolveImportReference(@NotNull QualifiedName name, @NotNull PyQualifiedNameResolveContext context, boolean withRoot) {
        List<String> components = name.getComponents();
        if (components.size() < 1 || !components.get(0).equals("odoo")) {
            return null;
        }
        Project project = context.getProject();
        if (components.size() > 2 && components.get(1).equals("addons")) {
            String moduleName = components.get(2);
            PsiDirectory module = OdooModuleIndex.getModule(moduleName, project);
            QualifiedName relatedName = name.subQualifiedName(3, name.getComponentCount());
            List<PsiElement> refs = PyResolveImportUtil.resolveModuleAt(relatedName, module, context);
            if (!refs.isEmpty()) {
                return refs.get(0);
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
