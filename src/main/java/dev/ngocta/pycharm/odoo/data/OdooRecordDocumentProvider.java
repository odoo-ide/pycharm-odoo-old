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
            OdooRecordElement recordElement = (OdooRecordElement) element;
            return DocumentationMarkup.DEFINITION_START +
                    recordElement.getPresentableText() +
                    DocumentationMarkup.GRAYED_START +
                    " (" + recordElement.getLocationString() + ")" +
                    DocumentationMarkup.GRAYED_END +
                    DocumentationMarkup.DEFINITION_END;
        }
        return null;
    }

    @Override
    @Nullable
    public String getQuickNavigateInfo(PsiElement element,
                                       PsiElement originalElement) {
        return generateDoc(element, originalElement);
    }
}
