package dev.ngocta.pycharm.odoo;

import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.psi.resolve.PyCanonicalPathProvider;
import com.jetbrains.python.psi.resolve.PyResolveImportUtil;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import dev.ngocta.pycharm.odoo.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooCanonicalPathProvider implements PyCanonicalPathProvider {
    @Nullable
    @Override
    public QualifiedName getCanonicalPath(@NotNull QualifiedName qualifiedName,
                                          @Nullable PsiElement psiElement) {
        if (psiElement == null) {
            return null;
        }
        List<PsiElement> elements = PyResolveImportUtil.resolveQualifiedName(qualifiedName, PyResolveImportUtil.fromFoothold(psiElement));
        if (elements.size() != 1) {
            return null;
        }
        PsiElement element = elements.get(0);
        OdooModule module = OdooModuleUtils.getContainingOdooModule(element);
        if (module == null) {
            return null;
        }
        String moduleName = module.getName();
        List<String> components = qualifiedName.getComponents();
        int moduleNamePos = components.indexOf(moduleName);
        if (moduleNamePos < 0) {
            return null;
        }
        QualifiedName relativeNameFromModule = qualifiedName.subQualifiedName(moduleNamePos, components.size());
        return QualifiedName.fromDottedString("odoo.addons").append(relativeNameFromModule);
    }
}
