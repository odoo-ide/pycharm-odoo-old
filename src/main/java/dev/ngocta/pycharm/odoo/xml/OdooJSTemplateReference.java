package dev.ngocta.pycharm.odoo.xml;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PlatformIcons;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.dom.js.OdooDomJSTemplate;
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
        String moduleName = null;
        if (myIsQualified) {
            String[] splits = name.split("\\.", 2);
            if (splits.length > 1) {
                name = splits[1];
                moduleName = splits[0];
            }
        }
        List<PsiElement> elements = new LinkedList<>();
        List<OdooDomJSTemplate> templates = OdooJSTemplateIndex.findTemplatesByName(name, getElement());
        for (OdooDomJSTemplate template : templates) {
            XmlTag xmlTag = template.getXmlTag();
            if (xmlTag != null) {
                if (moduleName != null) {
                    OdooModule module = OdooModuleUtils.getContainingOdooModule(xmlTag);
                    if (module != null && !module.getName().equals(moduleName)) {
                        continue;
                    }
                }
                OdooJSTemplateElement element = template.getNavigationElement();
                if (element != null) {
                    elements.add(element);
                }
            }
        }
        return PsiElementResolveResult.createResults(elements);
    }

    @Override
    @NotNull
    public Object[] getVariants() {
        List<Object> variants = new LinkedList<>();
        List<String> names = OdooJSTemplateIndex.getAvailableTemplateNames(getElement());
        for (String name : names) {
            LookupElement lookupElement = LookupElementBuilder.create(name).withIcon(PlatformIcons.XML_TAG_ICON);
            variants.add(lookupElement);
        }
        return variants.toArray();
    }
}
