package dev.ngocta.pycharm.odoo.xml.dom.js;

import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import org.jetbrains.annotations.NotNull;

public class OdooDomJSTemplateExtender extends DomExtender<OdooDomJSTemplateElement> {
    @Override
    public void registerExtensions(@NotNull OdooDomJSTemplateElement templateElement,
                                   @NotNull DomExtensionsRegistrar registrar) {
        if (templateElement instanceof OdooDomJSTemplateFile) {
            for (String tagName : OdooXmlUtils.getChildTagNames(templateElement)) {
                registrar.registerCollectionChildrenExtension(new XmlName(tagName), OdooDomJSTemplate.class);
            }
        }
    }
}
