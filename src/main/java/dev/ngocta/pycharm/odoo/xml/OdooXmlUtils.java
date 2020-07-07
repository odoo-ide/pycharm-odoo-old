package dev.ngocta.pycharm.odoo.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.vfs.VirtualFile;
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
    public static final PatternCondition<PsiElement> ODOO_XML_ELEMENT_PATTERN_CONDITION =
            new PatternCondition<PsiElement>("odooXmlElement") {
                @Override
                public boolean accepts(@NotNull PsiElement element,
                                       ProcessingContext context) {
                    return inOdooXmlFile(element);
                }
            };

    private OdooXmlUtils() {
    }

    @Nullable
    public static OdooDomRoot getOdooDomRoot(@NotNull XmlFile xmlFile) {
        DomManager domManager = DomManager.getDomManager(xmlFile.getProject());
        DomFileElement<OdooDomRoot> fileElement = domManager.getFileElement(xmlFile, OdooDomRoot.class);
        if (fileElement != null) {
            return fileElement.getRootElement();
        }
        return null;
    }

    public static boolean isXmlFile(@NotNull VirtualFile file) {
        return FileTypeRegistry.getInstance().isFileOfType(file, XmlFileType.INSTANCE);
    }

    public static boolean isOdooXmlFile(@NotNull PsiFile file) {
        return file instanceof XmlFile && OdooXmlUtils.getOdooDomRoot((XmlFile) file) != null;
    }

    public static boolean inOdooXmlFile(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        return file != null && isOdooXmlFile(file);
    }

    @Nullable
    public static String getViewInheritId(@NotNull OdooDomRecordLike record) {
        if (record instanceof OdooDomRecord) {
            for (OdooDomFieldAssignment field : ((OdooDomRecord) record).getFields()) {
                if ("inherit_id".equals(field.getName().getStringValue())) {
                    return field.getRef().getStringValue();
                }
            }
        } else if (record instanceof OdooDomTemplate) {
            return ((OdooDomTemplate) record).getInheritId().getStringValue();
        }
        return null;
    }
}
