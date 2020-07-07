package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class OdooModuleDocumentationProvider implements DocumentationProvider {
    @Nullable
    @Override
    public String generateDoc(PsiElement element,
                              @Nullable PsiElement originalElement) {
        if (element instanceof PsiDirectory) {
            PsiDirectory dir = (PsiDirectory) element;
            if (OdooModuleUtils.isOdooModuleDirectory(dir.getVirtualFile())) {
                OdooModule module = new OdooModule(dir);
                OdooManifestInfo info = module.getManifestInfo();
                if (info == null || info.getName() == null) {
                    return null;
                }
                String doc = DocumentationMarkup.DEFINITION_START + info.getName() + DocumentationMarkup.DEFINITION_END;
                if (info.getSummary() != null && !info.getSummary().isEmpty()) {
                    doc += DocumentationMarkup.CONTENT_START + info.getSummary() + DocumentationMarkup.CONTENT_END;
                }
                return doc;
            }
        }
        return null;
    }
}
