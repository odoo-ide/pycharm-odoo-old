package dev.ngocta.pycharm.odoo.data;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.Nullable;

public class OdooRecordDocumentProvider implements DocumentationProvider {
    @Nullable
    @Override
    public String generateDoc(PsiElement element,
                              @Nullable PsiElement originalElement) {
        if (element instanceof OdooRecordElement) {
            return DocumentationMarkup.DEFINITION_START +
                    ((OdooRecordElement) element).getPresentableText() +
                    DocumentationMarkup.DEFINITION_END;
        }
        return null;
    }
}
