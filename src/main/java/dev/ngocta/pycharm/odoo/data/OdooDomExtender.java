package dev.ngocta.pycharm.odoo.data;

import com.google.common.collect.ImmutableMap;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.XmlName;
import com.intellij.util.xml.reflect.DomExtender;
import com.intellij.util.xml.reflect.DomExtensionsRegistrar;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
                if (OdooDataUtils.getViewInheritId((OdooDomRecord) parent) != null) {
                    registerInheritLocators(domElement, registrar);
                    return;
                }
            }
            registerModelScopedViewElements(domElement, registrar);
        } else if (domElement instanceof OdooDomTemplate) {
            if (OdooDataUtils.getViewInheritId((OdooDomTemplate) domElement) != null) {
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
                && "arch".equals(((OdooDomFieldAssignment) domElement).getName().getStringValue())
                && OdooNames.IR_UI_VIEW.equals(((OdooDomFieldAssignment) domElement).getModel());
    }

    private Set<String> getChildTagNames(@NotNull OdooDomElement domElement) {
        XmlElement element = domElement.getXmlElement();
        return PsiTreeUtil.getChildrenOfTypeAsList(element, XmlTag.class)
                .stream()
                .map(XmlTag::getName)
                .collect(Collectors.toSet());
    }

    private void registerOperations(@NotNull OdooDomElement domElement,
                                    @NotNull DomExtensionsRegistrar registrar) {
        getChildTagNames(domElement).forEach(name -> {
            Type type = OPERATION_TYPES.get(name);
            if (type != null) {
                registrar.registerCollectionChildrenExtension(new XmlName(name), type);
            }
        });
    }

    private void registerInheritLocators(@NotNull OdooDomElement domElement,
                                         @NotNull DomExtensionsRegistrar registrar) {
        getChildTagNames(domElement).forEach(name -> {
            Type type = "xpath".equals(name) ? OdooDomViewXPath.class : OdooDomViewInheritLocator.class;
            registrar.registerCollectionChildrenExtension(new XmlName(name), type);
        });
    }

    private void registerViewElements(@NotNull OdooDomElement domElement,
                                      @NotNull DomExtensionsRegistrar registrar) {
        getChildTagNames(domElement).forEach(name -> {
            registrar.registerCollectionChildrenExtension(new XmlName(name), OdooDomViewElement.class);
        });
    }

    private void registerModelScopedViewElements(@NotNull OdooDomElement domElement,
                                                 @NotNull DomExtensionsRegistrar registrar) {
        getChildTagNames(domElement).forEach(name -> {
            Type type = MODEL_SCOPED_VIEW_ELEMENT_TYPES.getOrDefault(name, OdooDomModelScopedViewElement.class);
            registrar.registerCollectionChildrenExtension(new XmlName(name), type);
        });
    }
}
