package dev.ngocta.pycharm.odoo.xml;

import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.usageView.UsageViewTypeLocation;
import com.intellij.util.ObjectUtils;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomTarget;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomJSTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooJSTemplateElementDescriptionProvider implements ElementDescriptionProvider {
    @Override
    @Nullable
    public String getElementDescription(@NotNull PsiElement element,
                                        @NotNull ElementDescriptionLocation location) {
        OdooDomJSTemplate template = null;
        if (element instanceof PomTargetPsiElement) {
            PomTarget target = ((PomTargetPsiElement) element).getTarget();
            if (target instanceof DomTarget) {
                template = ObjectUtils.tryCast(((DomTarget) target).getDomElement(), OdooDomJSTemplate.class);
            }
        } else if (element instanceof XmlTag) {
            DomElement domElement = DomManager.getDomManager(element.getProject()).getDomElement((XmlTag) element);
            template = ObjectUtils.tryCast(domElement, OdooDomJSTemplate.class);
        }
        if (template != null) {
            if (location instanceof UsageViewTypeLocation) {
                return "template";
            }
        }
        return null;
    }
}
