package dev.ngocta.pycharm.odoo.xml;

import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import dev.ngocta.pycharm.odoo.xml.dom.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooXmlUtils {
    public static final PatternCondition<PsiElement> ODOO_XML_DATA_ELEMENT_PATTERN_CONDITION =
            new PatternCondition<PsiElement>("odooDataXmlElement") {
                @Override
                public boolean accepts(@NotNull PsiElement element,
                                       ProcessingContext context) {
                    return inOdooXmlDataFile(element);
                }
            };

    private OdooXmlUtils() {
    }

    @Nullable
    public static OdooDomRoot getOdooDataDomRoot(@NotNull XmlFile xmlFile) {
        DomManager domManager = DomManager.getDomManager(xmlFile.getProject());
        DomFileElement<OdooDomRoot> fileElement = domManager.getFileElement(xmlFile, OdooDomRoot.class);
        if (fileElement != null) {
            return fileElement.getRootElement();
        }
        return null;
    }

    public static boolean isOdooXmlDataFile(@NotNull PsiFile file) {
        return file instanceof XmlFile && OdooXmlUtils.getOdooDataDomRoot((XmlFile) file) != null;
    }

    public static boolean inOdooXmlDataFile(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        return file != null && isOdooXmlDataFile(file);
    }

    @Nullable
    public static String getViewInheritId(@NotNull OdooDomRecordLike record) {
        if (record instanceof OdooDomRecord) {
            OdooDomFieldAssignment field = ((OdooDomRecord) record).findField("inherit_id");
            if (field != null) {
                return field.getRefAttr().getStringValue();
            }
        } else if (record instanceof OdooDomTemplate) {
            return ((OdooDomTemplate) record).getInheritId();
        }
        return null;
    }
}
