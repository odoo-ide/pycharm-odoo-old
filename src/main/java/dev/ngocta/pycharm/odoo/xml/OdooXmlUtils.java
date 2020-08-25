package dev.ngocta.pycharm.odoo.xml;

import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomUtil;
import dev.ngocta.pycharm.odoo.data.OdooRecordExtraInfo;
import dev.ngocta.pycharm.odoo.data.OdooRecordViewInfo;
import dev.ngocta.pycharm.odoo.xml.dom.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class OdooXmlUtils {
    private OdooXmlUtils() {
    }

    public static final PatternCondition<PsiElement> ODOO_XML_DATA_ELEMENT_PATTERN_CONDITION =
            new PatternCondition<PsiElement>("odooXmlDataElement") {
                @Override
                public boolean accepts(@NotNull PsiElement element,
                                       ProcessingContext context) {
                    return isOdooXmlDataElement(element);
                }
            };

    public static final PatternCondition<PsiElement> ODOO_JS_TEMPLATE_ELEMENT_PATTERN_CONDITION =
            new PatternCondition<PsiElement>("odooJSTemplateElement") {
                @Override
                public boolean accepts(@NotNull PsiElement element,
                                       ProcessingContext context) {
                    return isOdooJSTemplateElement(element);
                }
            };

    public static final PatternCondition<PsiElement> ODOO_XML_ELEMENT_PATTERN_CONDITION =
            new PatternCondition<PsiElement>("odooXmlElement") {
                @Override
                public boolean accepts(@NotNull PsiElement element,
                                       ProcessingContext context) {
                    return DomUtil.getDomElement(element) instanceof OdooDomElement;
                }
            };

    public static final Pattern XML_ATTR_VALUE_RE_PATTERN = Pattern.compile("\\s*(.*\\S)\\s*", Pattern.DOTALL);
    public static final Pattern XML_ATTR_VALUE_RE_PATTERN_FORMAT_STRING = Pattern.compile("(?:#\\{\\s*((?!.*&(amp|lt|gt|quot|apos);).+?)\\s*})|(?:\\{\\{\\s*(.+?)\\s*}})", Pattern.DOTALL);

    @Nullable
    public static <T extends DomElement> T getDomFile(@Nullable PsiElement element,
                                                      @NotNull Class<T> cls) {
        if (element == null) {
            return null;
        }
        if (!(element instanceof PsiFile)) {
            return getDomFile(element.getContainingFile(), cls);
        }
        if (!(element instanceof XmlFile)) {
            return null;
        }
        XmlFile xmlFile = (XmlFile) element;
        DomManager domManager = DomManager.getDomManager(xmlFile.getProject());
        DomFileElement<T> fileElement = domManager.getFileElement(xmlFile, cls);
        if (fileElement != null) {
            return fileElement.getRootElement();
        }
        return null;
    }

    @Nullable
    public static OdooDomDataFile getOdooDataDomFile(@Nullable PsiElement element) {
        return getDomFile(element, OdooDomDataFile.class);
    }

    @Nullable
    public static OdooDomJSTemplateFile getOdooDomJSTemplateFile(@Nullable PsiElement element) {
        return getDomFile(element, OdooDomJSTemplateFile.class);
    }

    public static boolean isOdooXmlDataElement(@Nullable PsiElement element) {
        return OdooXmlUtils.getOdooDataDomFile(element) != null;
    }

    public static boolean isOdooJSTemplateElement(@Nullable PsiElement element) {
        return getOdooDomJSTemplateFile(element) != null;
    }

    @Nullable
    public static String getViewInheritId(@Nullable DomElement viewDefinition) {
        if (viewDefinition instanceof OdooDomRecord) {
            OdooRecordExtraInfo extraInfo = ((OdooDomRecord) viewDefinition).getRecordExtraInfo();
            if (extraInfo instanceof OdooRecordViewInfo) {
                return ((OdooRecordViewInfo) extraInfo).getInheritId();
            }
        } else if (viewDefinition instanceof OdooDomTemplate) {
            return ((OdooDomTemplate) viewDefinition).getInheritId();
        } else if (viewDefinition instanceof OdooDomJSTemplate) {
            return ((OdooDomJSTemplate) viewDefinition).getInheritName();
        }
        return null;
    }
}
