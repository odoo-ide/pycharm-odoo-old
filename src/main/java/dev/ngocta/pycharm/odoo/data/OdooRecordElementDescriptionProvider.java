package dev.ngocta.pycharm.odoo.data;

import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageViewTypeLocation;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomTarget;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRecordLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooRecordElementDescriptionProvider implements ElementDescriptionProvider {
    @Override
    @Nullable
    public String getElementDescription(@NotNull PsiElement element,
                                        @NotNull ElementDescriptionLocation location) {
        if (element instanceof OdooRecordElement) {
            if (location instanceof UsageViewTypeLocation) {
                if (element.getOriginalElement() instanceof XmlTag) {
                    return ((XmlTag) element.getOriginalElement()).getName();
                }
                return ((OdooRecordElement) element).getRecord().getModel();
            }
        } else if (element instanceof XmlTag && OdooXmlUtils.isOdooXmlDataElement(element)) {
            if (location instanceof UsageViewTypeLocation) {
                return ((XmlTag) element).getName();
            }
        } else if (element instanceof PomTargetPsiElement) {
            PomTarget target = ((PomTargetPsiElement) element).getTarget();
            if (target instanceof DomTarget) {
                DomElement domElement = ((DomTarget) target).getDomElement();
                if (domElement instanceof OdooDomRecordLike) {
                    if (location instanceof UsageViewTypeLocation) {
                        return domElement.getXmlElementName();
                    }
                }
            }
        }
        return null;
    }
}
