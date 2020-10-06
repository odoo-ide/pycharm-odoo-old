package dev.ngocta.pycharm.odoo.xml;

import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageViewTypeLocation;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.ElementPresentation;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooXmlElementDescriptionProvider implements ElementDescriptionProvider {
    @Override
    @Nullable
    public String getElementDescription(@NotNull PsiElement element,
                                        @NotNull ElementDescriptionLocation location) {
        if (element instanceof XmlTag) {
            DomElement domElement = DomManager.getDomManager(element.getProject()).getDomElement((XmlTag) element);
            if (domElement instanceof OdooDomElement) {
                ElementPresentation presentation = domElement.getPresentation();
                if (location instanceof UsageViewTypeLocation) {
                    return presentation.getTypeName();
                }
            }
        }
        return null;
    }
}
