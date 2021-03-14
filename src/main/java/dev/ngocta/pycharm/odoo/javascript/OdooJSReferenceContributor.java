package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.psi.*;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ProcessingContext;
import dev.ngocta.pycharm.odoo.OdooFilePathReferenceProvider;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.OdooJSTemplateReferenceProvider;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooJSReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<JSLiteralExpression> FILE_PATTERN =
            psiElement(JSLiteralExpression.class).with(new PatternCondition<JSLiteralExpression>("file") {
                @Override
                public boolean accepts(@NotNull JSLiteralExpression jsLiteralExpression,
                                       ProcessingContext context) {
                    String value = jsLiteralExpression.getStringValue();
                    if (value == null || !value.startsWith("/")) {
                        return false;
                    }
                    PsiElement parent = jsLiteralExpression.getParent();
                    if (parent instanceof JSArrayLiteralExpression) {
                        parent = parent.getParent();
                        if (parent instanceof JSArgumentList) {
                            parent = parent.getParent();
                            if (parent instanceof JSCallExpression) {
                                parent = parent.getParent();
                            }
                        }
                        if (parent instanceof JSProperty) {
                            String name = ((JSProperty) parent).getName();
                            return ArrayUtil.contains(name, "xmlDependencies", "jsLibs", "cssLibs") && OdooModuleUtils.isInOdooModule(parent);
                        }
                    }
                    return false;
                }
            });

    public static final PsiElementPattern.Capture<JSLiteralExpression> WIDGET_TEMPLATE_PATTERN =
            psiElement(JSLiteralExpression.class).with(new PatternCondition<JSLiteralExpression>("widgetTemplate") {
                @Override
                public boolean accepts(@NotNull JSLiteralExpression jsLiteralExpression,
                                       ProcessingContext context) {
                    PsiElement parent = jsLiteralExpression.getParent();
                    if (parent instanceof JSProperty) {
                        String name = ((JSProperty) parent).getName();
                        return "template".equals(name) && OdooModuleUtils.isInOdooModule(parent);
                    }
                    return false;
                }
            });

    public static final PsiElementPattern.Capture<JSLiteralExpression> QWEB_RENDER_TEMPLATE_PATTERN =
            psiElement(JSLiteralExpression.class).with(new PatternCondition<JSLiteralExpression>("qwebRenderTemplate") {
                @Override
                public boolean accepts(@NotNull JSLiteralExpression jsLiteralExpression,
                                       ProcessingContext context) {
                    PsiElement parent = jsLiteralExpression.getParent();
                    if (parent instanceof JSArgumentList) {
                        JSExpression[] args = ((JSArgumentList) parent).getArguments();
                        if (args.length > 0 && args[0].equals(jsLiteralExpression)) {
                            parent = parent.getParent();
                            if (parent instanceof JSCallExpression) {
                                PsiElement callee = parent.getFirstChild();
                                if (callee instanceof JSReferenceExpression) {
                                    if ("render".equals(((JSReferenceExpression) callee).getReferenceName())) {
                                        JSExpression qualifier = ((JSReferenceExpression) callee).getQualifier();
                                        if (qualifier instanceof JSReferenceExpression) {
                                            String qualifierName = ((JSReferenceExpression) qualifier).getReferenceName();
                                            return qualifierName != null && qualifierName.toLowerCase().contains("qweb");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }
            });

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(FILE_PATTERN, new OdooFilePathReferenceProvider());
        registrar.registerReferenceProvider(WIDGET_TEMPLATE_PATTERN, new OdooJSTemplateReferenceProvider());
        registrar.registerReferenceProvider(QWEB_RENDER_TEMPLATE_PATTERN, new OdooJSTemplateReferenceProvider());
    }
}
