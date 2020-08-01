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
            String model = ((OdooRecordElement) element).getRecord().getModel();
            return DocumentationMarkup.DEFINITION_START + "A record of model <b>" + model + "</b>" + DocumentationMarkup.DEFINITION_END;
        }
        return null;
    }
}
