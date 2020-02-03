package dev.ngocta.pycharm.odoo.data;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyFunctionType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.model.OdooModelUtils;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooExternalIdReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> REF_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(
                    psiElement(PyArgumentList.class).withParent(
                            psiElement(PyCallExpression.class).with(new PatternCondition<PyCallExpression>("ref") {
                                @Override
                                public boolean accepts(@NotNull PyCallExpression call, ProcessingContext context) {
                                    PyExpression callee = call.getCallee();
                                    if (callee != null) {
                                        TypeEvalContext typeEvalContext = TypeEvalContext.codeAnalysis(call.getProject(), call.getContainingFile());
                                        PyType calleeType = typeEvalContext.getType(callee);
                                        if (calleeType instanceof PyFunctionType) {
                                            PyCallable callable = ((PyFunctionType) calleeType).getCallable();
                                            return callable instanceof PyFunction && OdooNames.REF_QNAME.equals(callable.getQualifiedName());
                                        }
                                    }
                                    return false;
                                }
                            })));

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> REQUEST_RENDER_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("requestRender") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression, ProcessingContext context) {
                    PsiElement parent = pyStringLiteralExpression.getParent();
                    if (parent instanceof PyArgumentList) {
                        PyExpression[] args = ((PyArgumentList) parent).getArguments();
                        if (args.length > 0 && pyStringLiteralExpression == args[0]) {
                            PyCallExpression callExpression = ((PyArgumentList) parent).getCallExpression();
                            if (callExpression != null) {
                                PyExpression callee = callExpression.getCallee();
                                if (callee instanceof PyReferenceExpression && "render".equals(callee.getName())) {
                                    PsiElement target = ((PyReferenceExpression) callee).getReference().resolve();
                                    if (target instanceof PyFunction && OdooNames.REQUEST_RENDER_QNAME.equals(((PyFunction) target).getQualifiedName())) {
                                        context.put(OdooExternalIdReferenceProvider.SUB_TYPE, OdooRecordSubType.VIEW_QWEB);
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    return false;
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> FIELD_ATTR_GROUPS_PATTERN =
            OdooModelUtils.getFieldArgumentPattern(-1, "groups").with(new PatternCondition<PyStringLiteralExpression>("fieldGroups") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression, ProcessingContext context) {
                    context.put(OdooExternalIdReferenceProvider.MODEL, OdooNames.RES_GROUPS);
                    context.put(OdooExternalIdReferenceProvider.COMMA_SEPARATED, true);
                    return true;
                }
            });

    public static final XmlAttributeValuePattern T_CALL_PATTERN =
            XmlPatterns.xmlAttributeValue("t-call", "t-call-assets")
                    .with(OdooDataUtils.ODOO_XML_ELEMENT_PATTERN_CONDITION)
                    .with(new PatternCondition<XmlAttributeValue>("tCall") {
                        @Override
                        public boolean accepts(@NotNull XmlAttributeValue xmlAttributeValue, ProcessingContext context) {
                            context.put(OdooExternalIdReferenceProvider.SUB_TYPE, OdooRecordSubType.VIEW_QWEB);
                            return true;
                        }
                    });

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        OdooExternalIdReferenceProvider provider = new OdooExternalIdReferenceProvider();
        registrar.registerReferenceProvider(REF_PATTERN, provider);
        registrar.registerReferenceProvider(REQUEST_RENDER_PATTERN, provider);
        registrar.registerReferenceProvider(FIELD_ATTR_GROUPS_PATTERN, provider);
        registrar.registerReferenceProvider(T_CALL_PATTERN, provider);
    }
}
