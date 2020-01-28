package dev.ngocta.pycharm.odoo.data;

import com.google.common.collect.ImmutableMap;
import com.intellij.psi.xml.XmlTag;
import org.intellij.lang.xpath.context.ContextType;
import org.intellij.lang.xpath.context.functions.Function;
import org.intellij.lang.xpath.context.functions.Parameter;
import org.intellij.lang.xpath.context.functions.XPathFunctionProvider;
import org.intellij.lang.xpath.psi.XPathType;
import org.intellij.plugins.xpathView.support.jaxen.extensions.FunctionImplementation;
import org.jetbrains.annotations.NotNull;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OdooXPathFunctionProvider extends XPathFunctionProvider {
    @NotNull
    @Override
    public Map<QName, ? extends Function> getFunctions(ContextType contextType) {
        return ImmutableMap.<QName, Function>builder()
                .put(new QName(XPathFunctionHasClass.NAME), new XPathFunctionHasClass())
                .build();
    }

    static class XPathFunctionHasClass extends FunctionImplementation {
        public static final String NAME = "hasclass";

        public XPathFunctionHasClass() {
            super(NAME, XPathType.BOOLEAN, new Parameter(XPathType.STRING, Parameter.Kind.VARARG));
        }

        @Override
        public org.jaxen.Function getImplementation() {
            return (context, list) -> {
                if (list.isEmpty()) {
                    return Boolean.FALSE;
                }
                for (Object item : context.getNodeSet()) {
                    if (item instanceof XmlTag) {
                        String classAttr = ((XmlTag) item).getAttributeValue("class");
                        if (classAttr == null) {
                            return Boolean.FALSE;
                        }
                        List<String> classes = Arrays.asList(classAttr.split("\\s+"));
                        for (Object cls : list) {
                            if (!classes.contains(cls)) {
                                return Boolean.FALSE;
                            }
                        }
                        return Boolean.TRUE;
                    }
                }
                return Boolean.FALSE;
            };
        }
    }
}
