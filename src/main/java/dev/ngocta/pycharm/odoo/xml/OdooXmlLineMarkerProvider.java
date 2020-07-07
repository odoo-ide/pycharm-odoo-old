package dev.ngocta.pycharm.odoo.xml;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.xml.util.IncludedXmlTag;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomViewInheritLocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooXmlLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        ASTNode node = element.getNode();
        if (node != null && node.getElementType() == XmlTokenType.XML_START_TAG_START && element.getParent() instanceof XmlTag) {
            Project project = element.getProject();
            DomElement domElement = DomManager.getDomManager(project).getDomElement((XmlTag) element.getParent());
            if (domElement instanceof OdooDomViewInheritLocator) {
                return getInheritedElementsLineMarker(element, (OdooDomViewInheritLocator) domElement);
            }
        }
        return null;
    }

    private LineMarkerInfo<PsiElement> getInheritedElementsLineMarker(PsiElement identifier,
                                                                      OdooDomViewInheritLocator locator) {
        if (locator.getInheritedElement() == null) {
            return null;
        }
        return new LineMarkerInfo<>(identifier,
                identifier.getTextRange(),
                AllIcons.Gutter.OverridingMethod,
                o -> "View inherited element",
                (e, elt) -> {
                    XmlTag inheritedElement = locator.getInheritedElement();
                    if (inheritedElement != null) {
                        (new XmlTagWrapper(inheritedElement)).navigate(true);
                    }
                },
                GutterIconRenderer.Alignment.LEFT);
    }

    static class XmlTagWrapper extends IncludedXmlTag {
        XmlTagWrapper(XmlTag xmlTag) {
            super(xmlTag, xmlTag.getParent());
        }

        @Override
        public TextRange getTextRange() {
            return getOriginal().getTextRange();
        }
    }
}
