package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.model.OdooRecordSetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdooHttpRouteFunctionParamTypeProvider extends PyTypeProviderBase {
    private static final Pattern CONVERTER_PATTERN = Pattern.compile("<(.+?)>", Pattern.DOTALL);

    @Nullable
    @Override
    public Ref<PyType> getParameterType(@NotNull PyNamedParameter param,
                                        @NotNull PyFunction func,
                                        @NotNull TypeEvalContext context) {
        String paramName = param.getName();
        if (paramName == null) {
            return null;
        }
        PyDecoratorList decoratorList = func.getDecoratorList();
        PyDecorator decorator = getRouteDecorator(decoratorList);
        if (decorator == null) {
            return null;
        }
        PsiElement route = decorator.getArgument(0, "route", PsiElement.class);
        Map<String, String> variables = getRouteVariableWithConverters(route);
        if (variables.containsKey(paramName)) {
            String converter = variables.get(paramName);
            PyType type = getConverterType(converter, func);
            if (type != null) {
                return Ref.create(type);
            }
        }
        return null;
    }

    @Nullable
    private PyDecorator getRouteDecorator(@Nullable PyDecoratorList decoratorList) {
        if (decoratorList == null) {
            return null;
        }
        for (PyDecorator decorator : decoratorList.getDecorators()) {
            if (isRouteDecorator(decorator)) {
                return decorator;
            }
        }
        return null;
    }

    private boolean isRouteDecorator(@Nullable PyDecorator decorator) {
        if (decorator == null) {
            return false;
        }
        PsiElement element = Optional.of(decorator)
                .map(PyDecorator::getCallee)
                .map(PsiElement::getReference)
                .map(PsiReference::resolve)
                .orElse(null);
        if (element instanceof PyFunction) {
            PyFunction func = (PyFunction) element;
            return OdooNames.HTTP_ROUTE_QNAME.equals(func.getQualifiedName());
        }
        return false;
    }

    private Map<String, String> getRouteVariableWithConverters(PsiElement route) {
        if (route instanceof PyStringLiteralExpression) {
            return getRouteVariableWithConverters(((PyStringLiteralExpression) route).getStringValue());
        }
        if (route instanceof PySequenceExpression) {
            Map<String, String> variables = new HashMap<>();
            for (PyExpression element : ((PySequenceExpression) route).getElements()) {
                if (element instanceof PyStringLiteralExpression) {
                    variables.putAll(getRouteVariableWithConverters(element));
                }
            }
            return variables;
        }
        return Collections.emptyMap();
    }

    private Map<String, String> getRouteVariableWithConverters(String route) {
        Map<String, String> variables = new HashMap<>();
        Matcher matcher = CONVERTER_PATTERN.matcher(route);
        while (matcher.find()) {
            String s = matcher.group(1);
            int sepPos = s.lastIndexOf(':');
            if (sepPos < 0) {
                continue;
            }
            variables.put(s.substring(sepPos + 1), s.substring(0, sepPos));
        }
        return variables;
    }

    private PyType getConverterType(String converter,
                                    PsiElement anchor) {
        PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(anchor);
        if ("int".equals(converter) || converter.startsWith("int(")) {
            return builtinCache.getIntType();
        }
        if ("string".equals(converter) || converter.startsWith("string(")) {
            return builtinCache.getStrType();
        }
        if ("float".equals(converter) || converter.startsWith("float(")) {
            return builtinCache.getFloatType();
        }
        if (converter.startsWith("model(")) {
            int endMarkPos = converter.indexOf('"', 7);
            if (endMarkPos > 0) {
                String model = converter.substring(7, endMarkPos);
                return new OdooModelClassType(model, OdooRecordSetType.MULTI, anchor.getProject());
            }
        }
        return null;
    }
}
