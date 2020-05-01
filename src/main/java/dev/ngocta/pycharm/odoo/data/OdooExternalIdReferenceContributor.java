package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.project.Project;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyFunctionType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.model.OdooFieldInfo;
import dev.ngocta.pycharm.odoo.model.OdooModelUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooExternalIdReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> REF_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("ref") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
                    PyExpression callee = Optional.of(pyStringLiteralExpression)
                            .map(PsiElement::getParent).filter(PyArgumentList.class::isInstance)
                            .map(PsiElement::getParent).filter(PyCallExpression.class::isInstance)
                            .map(element -> ((PyCallExpression) element).getCallee())
                            .orElse(null);
                    if (callee == null) {
                        return false;
                    }
                    PsiFile containingFile = callee.getContainingFile();
                    if (containingFile == null) {
                        return false;
                    }
                    Project project = pyStringLiteralExpression.getProject();
                    TypeEvalContext typeEvalContext = TypeEvalContext.userInitiated(project, containingFile);
                    PyType calleeType = typeEvalContext.getType(callee);
                    if (calleeType instanceof PyFunctionType) {
                        PyCallable callable = ((PyFunctionType) calleeType).getCallable();
                        return callable instanceof PyFunction && OdooNames.REF_QNAME.equals(callable.getQualifiedName());
                    }
                    if (!"ref".equals(callee.getName())) {
                        return false;
                    }
                    XmlTag tag = Optional.of(containingFile)
                            .map(PsiElement::getContext).filter(XmlAttributeValue.class::isInstance)
                            .map(PsiElement::getParent).filter(XmlAttribute.class::isInstance)
                            .filter(element -> "eval".equals(((XmlAttribute) element).getName()))
                            .map(element -> ((XmlAttribute) element).getParent())
                            .orElse(null);
                    if (tag == null) {
                        return false;
                    }
                    String model = null;
                    PyKeyValueExpression kv = PsiTreeUtil.getParentOfType(callee, PyKeyValueExpression.class);
                    if (kv != null) {
                        PsiElement key = kv.getKey();
                        if (key instanceof PyStringLiteralExpression) {
                            model = Optional.of(key)
                                    .map(PsiElement::getReference)
                                    .map(PsiReference::resolve)
                                    .filter(PyTargetExpression.class::isInstance)
                                    .map(f -> OdooFieldInfo.getInfo((PyTargetExpression) f))
                                    .map(OdooFieldInfo::getComodel)
                                    .orElse(null);
                        }
                    } else {
                        DomManager domManager = DomManager.getDomManager(project);
                        DomElement domElement = domManager.getDomElement(tag);
                        if (domElement instanceof OdooDomFieldAssignment) {
                            model = ((OdooDomFieldAssignment) domElement).getComodel();
                        }
                    }
                    if (model != null) {
                        context.put(OdooExternalIdReferenceProvider.MODEL, model);
                    }
                    context.put(OdooExternalIdReferenceProvider.ALLOW_RELATIVE, true);
                    return true;
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> REQUEST_RENDER_PATTERN =
            psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("requestRender") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
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
            OdooModelUtils.getFieldArgumentPattern(-1, OdooNames.FIELD_ATTR_GROUPS).with(new PatternCondition<PyStringLiteralExpression>("fieldGroups") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
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
                        public boolean accepts(@NotNull XmlAttributeValue xmlAttributeValue,
                                               ProcessingContext context) {
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
