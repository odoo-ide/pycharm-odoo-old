package dev.ngocta.pycharm.odoo.xml;

import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomUtil;
import dev.ngocta.pycharm.odoo.xml.dom.*;
import dev.ngocta.pycharm.odoo.xml.dom.js.OdooDomJSTemplateFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.stream.Collectors;

public class OdooXmlUtils {
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

    private OdooXmlUtils() {
    }

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
    public static OdooDomFile getOdooDataDomFile(@Nullable PsiElement element) {
        return getDomFile(element, OdooDomFile.class);
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

    public static Set<String> getChildTagNames(@NotNull DomElement domElement) {
        XmlElement element = domElement.getXmlElement();
        return PsiTreeUtil.getChildrenOfTypeAsList(element, XmlTag.class)
                .stream()
                .map(XmlTag::getName)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
