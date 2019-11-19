package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.impl.PyImportResolver;
import com.jetbrains.python.psi.resolve.PyQualifiedNameResolveContext;
import com.jetbrains.python.psi.resolve.PyResolveImportUtil;
import com.jetbrains.python.psi.resolve.QualifiedNameFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooImportResolver implements PyImportResolver {
    @Override
    public @Nullable PsiElement resolveImportReference(@NotNull QualifiedName qualifiedName, @NotNull PyQualifiedNameResolveContext pyQualifiedNameResolveContext, boolean b) {
        List<String> components = qualifiedName.getComponents();
        Project project = pyQualifiedNameResolveContext.getProject();
        if (components.size() > 2 && components.get(0).equals("odoo") && components.get(1).equals("addons")) {
            String moduleName = components.get(2);
            PsiDirectory dir = OdooModuleIndex.getModule(moduleName, project);
            QualifiedName importableQName = QualifiedNameFinder.findShortestImportableQName(dir);
            if (importableQName == null) {
                return null;
            }
            for (int i = 3; i < components.size(); i++) {
                importableQName = importableQName.append(components.get(i));
            }
            if (qualifiedName.equals(importableQName)) {
                return null;
            }
            pyQualifiedNameResolveContext = pyQualifiedNameResolveContext.copyWithoutForeign();
            List<PsiElement> refs = PyResolveImportUtil.resolveQualifiedName(importableQName, pyQualifiedNameResolveContext);
            if (refs.size() > 0) {
                return refs.get(0);
            }
        }
        return null;
    }
}
