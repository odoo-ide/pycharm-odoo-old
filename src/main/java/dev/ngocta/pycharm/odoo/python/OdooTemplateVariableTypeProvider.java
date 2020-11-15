package dev.ngocta.pycharm.odoo.python;

import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomViewElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooTemplateVariableTypeProvider extends PyTypeProviderBase {
    @Override
    @Nullable
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression,
                                             @NotNull TypeEvalContext context) {
        String name = referenceExpression.getName();
        if (!referenceExpression.isQualified()
                && ArrayUtil.contains(name, "env", "request")) {
            PsiFile file = referenceExpression.getContainingFile();
            if (file != null && file.getContext() instanceof XmlElement) {
                DomElement domElement = DomUtil.getDomElement(file.getContext());
                if (domElement instanceof OdooDomViewElement
                        && OdooNames.VIEW_TYPE_QWEB.equals(((OdooDomViewElement) domElement).getViewType())) {
                    if ("env".equals(name)) {
                        return OdooPyUtils.getEnvironmentType(file);
                    } else if ("request".equals(name)) {
                        return OdooPyUtils.getHttpRequestType(file);
                    }
                }
            }
        }
        return null;
    }
}
