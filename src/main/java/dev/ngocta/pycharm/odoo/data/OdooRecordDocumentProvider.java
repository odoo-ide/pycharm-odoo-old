package dev.ngocta.pycharm.odoo.data;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.Nullable;

public class OdooRecordDocumentProvider implements DocumentationProvider {
    @Nullable
    @Override
    public String generateDoc(PsiElement element,
                              @Nullable PsiElement originalElement) {
        String model = null;
        if (element instanceof XmlTag) {
            XmlTag tag = (XmlTag) element;
            DomElement domElement = DomManager.getDomManager(element.getProject()).getDomElement(tag);
            if (domElement instanceof OdooDomRecordLike) {
                OdooRecord record = ((OdooDomRecordLike) domElement).getRecord();
                if (record != null) {
                    model = record.getModel();
                }
            }
        } else if (element instanceof OdooCsvRecord) {
            OdooCsvRecord record = (OdooCsvRecord) element;
            model = record.getModel();
        }
        if (model != null) {
            return DocumentationMarkup.DEFINITION_START + "A record of model " + model + DocumentationMarkup.DEFINITION_END;
        }
        return null;
    }

    @Nullable
    @Override
    public String getQuickNavigateInfo(PsiElement element,
                                       PsiElement originalElement) {
        if (element instanceof OdooCsvRecord) {
            return ((OdooCsvRecord) element).getContainingVirtualFile().getName();
        }
        return null;
    }
}
