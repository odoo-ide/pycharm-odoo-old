package dev.ngocta.pycharm.odoo.data;

import com.intellij.lang.documentation.DocumentationMarkup;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomTarget;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRecordLike;
import org.jetbrains.annotations.Nullable;

public class OdooRecordDocumentProvider implements DocumentationProvider {
    @Nullable
    @Override
    public String generateDoc(PsiElement element,
                              @Nullable PsiElement originalElement) {
        OdooRecordElement recordElement = null;
        if (element instanceof PomTargetPsiElement) {
            PomTarget target = ((PomTargetPsiElement) element).getTarget();
            if (target instanceof DomTarget && ((DomTarget) target).getDomElement() instanceof OdooDomRecordLike) {
                OdooDomRecordLike domRecordLike = (OdooDomRecordLike) ((DomTarget) target).getDomElement();
                OdooRecord record = domRecordLike.getRecord();
                XmlTag tag = domRecordLike.getXmlTag();
                if (record != null && tag != null) {
                    recordElement = new OdooRecordElement(record, tag);
                }
            }
        } else if (element instanceof OdooRecordElement) {
            recordElement = (OdooRecordElement) element;
        }
        if (recordElement != null) {
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
