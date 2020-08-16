package dev.ngocta.pycharm.odoo.xml.dom;

import com.google.common.collect.ImmutableMap;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;

public class OdooDomExtender extends DomExtender<OdooDomElement> {
    private static final Map<String, Type> OPERATION_TYPES = ImmutableMap.<String, Type>builder()
            .put("record", OdooDomRecord.class)
            .put("template", OdooDomTemplate.class)
            .put("menuitem", OdooDomMenuItem.class)
            .put("report", OdooDomReport.class)
            .put("act_window", OdooDomActWindow.class)
            .build();
    private static final Map<String, Type> MODEL_SCOPED_VIEW_ELEMENT_TYPES = ImmutableMap.<String, Type>builder()
            .put("field", OdooDomViewField.class)
            .put("button", OdooDomViewButton.class)
            .build();

    @Override
    public void registerExtensions(@NotNull OdooDomElement domElement,
                                   @NotNull DomExtensionsRegistrar registrar) {
        if (domElement instanceof OdooDomOperationContainer) {
            registerOperations(domElement, registrar);
        } else if (isViewArchFieldAssignment(domElement)) {
            DomElement parent = domElement.getParent();
            if (parent instanceof OdooDomRecord) {
                if (OdooXmlUtils.getViewInheritId((OdooDomRecord) parent) != null) {
                    registerInheritLocators(domElement, registrar);
                    return;
                }
            }
            registerModelScopedViewElements(domElement, registrar);
        } else if (domElement instanceof OdooDomTemplate) {
            if (((OdooDomTemplate) domElement).getInheritId() != null) {
                registerInheritLocators(domElement, registrar);
            } else {
                registerViewElements(domElement, registrar);
            }
        } else if (domElement instanceof OdooDomModelScopedViewElement || isViewArchFieldAssignment(domElement.getParent())) {
            registerModelScopedViewElements(domElement, registrar);
        } else if (domElement instanceof OdooDomViewElement) {
            registerViewElements(domElement, registrar);
        }
    }

    private boolean isViewArchFieldAssignment(@Nullable DomElement domElement) {
        return domElement instanceof OdooDomFieldAssignment
                && "arch".equals(((OdooDomFieldAssignment) domElement).getName())
                && OdooNames.IR_UI_VIEW.equals(((OdooDomFieldAssignment) domElement).getModel());
    }

    private void registerOperations(@NotNull OdooDomElement domElement,
                                    @NotNull DomExtensionsRegistrar registrar) {
        OdooXmlUtils.getChildTagNames(domElement).forEach(name -> {
            Type type = OPERATION_TYPES.get(name);
            if (type != null) {
                registrar.registerCollectionChildrenExtension(new XmlName(name), type);
            }
        });
    }

    private void registerInheritLocators(@NotNull OdooDomElement domElement,
                                         @NotNull DomExtensionsRegistrar registrar) {
        OdooXmlUtils.getChildTagNames(domElement).forEach(name -> {
            Type type = "xpath".equals(name) ? OdooDomViewXPath.class : OdooDomViewInheritLocator.class;
            registrar.registerCollectionChildrenExtension(new XmlName(name), type);
        });
    }

    private void registerViewElements(@NotNull OdooDomElement domElement,
                                      @NotNull DomExtensionsRegistrar registrar) {
        OdooXmlUtils.getChildTagNames(domElement).forEach(name -> {
            registrar.registerCollectionChildrenExtension(new XmlName(name), OdooDomViewElement.class);
        });
    }

    private void registerModelScopedViewElements(@NotNull OdooDomElement domElement,
                                                 @NotNull DomExtensionsRegistrar registrar) {
        OdooXmlUtils.getChildTagNames(domElement).forEach(name -> {
            Type type = MODEL_SCOPED_VIEW_ELEMENT_TYPES.getOrDefault(name, OdooDomModelScopedViewElement.class);
            registrar.registerCollectionChildrenExtension(new XmlName(name), type);
        });
    }
}
