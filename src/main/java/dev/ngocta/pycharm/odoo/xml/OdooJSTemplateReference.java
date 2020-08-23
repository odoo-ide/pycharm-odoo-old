package dev.ngocta.pycharm.odoo.xml;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.util.PlatformIcons;
import com.intellij.util.xml.DomUtil;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomJSTemplate;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class OdooJSTemplateReference extends PsiReferenceBase.Poly<PsiElement> {
    private final boolean myIsQualified;

    public OdooJSTemplateReference(PsiElement psiElement,
                                   boolean isQualified) {
        super(psiElement);
        myIsQualified = isQualified;
    }

    @Override
    @NotNull
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        String name = getValue();
        OdooDomJSTemplate inTemplate = DomUtil.findDomElement(getElement(), OdooDomJSTemplate.class);
        List<PsiElement> elements = new LinkedList<>();
        List<OdooDomJSTemplate> templates = OdooJSTemplateIndex.findTemplatesByName(name, getElement(), myIsQualified);
        for (OdooDomJSTemplate t : templates) {
            if (t.equals(inTemplate)) {
                continue;
            }
            OdooJSTemplateElement element = t.getNavigationElement();
            if (element != null) {
                elements.add(element);
            }
        }
        return PsiElementResolveResult.createResults(elements);
    }

    @Override
    @NotNull
    public Object[] getVariants() {
        List<Object> variants = new LinkedList<>();
        List<String> names = OdooJSTemplateIndex.getAvailableTemplateNames(getElement(), myIsQualified);
        for (String name : names) {
            LookupElement lookupElement = LookupElementBuilder.create(name).withIcon(PlatformIcons.XML_TAG_ICON);
            variants.add(lookupElement);
        }
        return variants.toArray();
    }
}
