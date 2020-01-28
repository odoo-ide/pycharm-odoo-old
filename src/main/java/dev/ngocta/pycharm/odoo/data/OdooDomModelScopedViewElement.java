package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.project.Project;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.model.OdooFieldInfo;
import dev.ngocta.pycharm.odoo.model.OdooModelClass;

public interface OdooDomModelScopedViewElement extends OdooDomViewElement, OdooDomModelScoped {
    default String getModel() {
        XmlElement element = getXmlElement();
        if (element == null) {
            return null;
        }
        Project project = element.getProject();
        return PyUtil.getNullableParameterizedCachedValue(element, null, param -> {
            DomElement parent = getParent();
            if (parent instanceof OdooDomViewInheritLocator) {
                XmlTag inheritedElement = ((OdooDomViewInheritLocator) parent).getInheritedElement();
                if (inheritedElement != null) {
                    DomElement domElement = DomManager.getDomManager(project).getDomElement(inheritedElement);
                    if (domElement instanceof OdooDomModelScoped) {
                        return ((OdooDomModelScoped) domElement).getModel();
                    }
                }
                return null;
            } else if (parent instanceof OdooDomFieldAssignment) {
                parent = parent.getParent();
                if (parent instanceof OdooDomRecord) {
                    for (OdooDomFieldAssignment field : ((OdooDomRecord) parent).getFields()) {
                        if ("model".equals(field.getName().getStringValue())) {
                            return field.getStringValue();
                        }
                    }
                }
                return null;
            } else if (parent instanceof OdooDomViewField) {
                TypeEvalContext context = TypeEvalContext.codeAnalysis(project, element.getContainingFile());
                String model = ((OdooDomViewField) parent).getModel();
                String fieldName = ((OdooDomViewField) parent).getName().getStringValue();
                if (model != null && fieldName != null) {
                    PyTargetExpression field = OdooModelClass.getInstance(model, element.getProject()).findField(fieldName, context);
                    if (field != null) {
                        OdooFieldInfo info = OdooFieldInfo.getInfo(field);
                        if (info != null) {
                            return info.getComodel();
                        }
                    }
                }
                return null;
            } else if (parent instanceof OdooDomModelScoped) {
                return ((OdooDomModelScoped) parent).getModel();
            }
            return null;
        });
    }
}
