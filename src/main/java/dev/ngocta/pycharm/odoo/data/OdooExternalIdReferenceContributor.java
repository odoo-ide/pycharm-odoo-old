package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.project.Project;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.patterns.XmlAttributeValuePattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilters;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordModelFilter;
import dev.ngocta.pycharm.odoo.python.model.OdooFieldInfo;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClass;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomFieldAssignment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooExternalIdReferenceContributor extends PsiReferenceContributor {
    @Nullable
    private static String resolveRefModel(PsiElement refExpression) {
        Project project = refExpression.getProject();
        PsiFile containingFile = refExpression.getContainingFile();
        if (containingFile == null) {
            return null;
        }
        XmlTag tag = Optional.of(containingFile)
                .map(PsiElement::getContext).filter(XmlAttributeValue.class::isInstance)
                .map(PsiElement::getParent).filter(XmlAttribute.class::isInstance)
                .filter(element -> "eval".equals(((XmlAttribute) element).getName()))
                .map(element -> ((XmlAttribute) element).getParent())
                .orElse(null);
        if (tag == null) {
            return null;
        }
        String model = null;
        PyKeyValueExpression kv = PsiTreeUtil.getParentOfType(refExpression, PyKeyValueExpression.class);
        if (kv != null) {
            PsiElement key = kv.getKey();
            if (key instanceof PyStringLiteralExpression) {
                model = Optional.of(key)
                        .map(PsiElement::getReference)
                        .map(PsiReference::resolve)
                        .map(OdooFieldInfo::getInfo)
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
        return model;
    }


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
                    if (callee == null || !"ref".equals(callee.getName())) {
                        return false;
                    }
                    String model = resolveRefModel(callee);
                    context.put(OdooExternalIdReferenceProvider.FILTER, new OdooRecordModelFilter(model));
                    PsiFile contextFile = FileContextUtil.getContextFile(callee);
                    if (contextFile instanceof XmlFile) {
                        context.put(OdooExternalIdReferenceProvider.ALLOW_RELATIVE, true);
                    }
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
                                    if (target instanceof PyFunction && OdooNames.HTTP_REQUEST_RENDER_FUNC_QNAME.equals(((PyFunction) target).getQualifiedName())) {
                                        context.put(OdooExternalIdReferenceProvider.FILTER, OdooRecordFilters.QWEB);
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
            OdooModelUtils.getFieldAttributePattern(-1, OdooNames.FIELD_ATTR_GROUPS).with(new PatternCondition<PyStringLiteralExpression>("fieldGroups") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
                    context.put(OdooExternalIdReferenceProvider.FILTER, OdooRecordFilters.RES_GROUPS);
                    context.put(OdooExternalIdReferenceProvider.COMMA_SEPARATED, true);
                    return true;
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> RES_CONFIG_FIELD_ATTR_GROUP_PATTERN =
            OdooModelUtils.getFieldAttributePattern(-1, "group").with(new PatternCondition<PyStringLiteralExpression>("fieldGroup") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
                    OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(pyStringLiteralExpression);
                    if (modelClass != null && OdooNames.RES_CONFIG_SETTINGS.equals(modelClass.getName())) {
                        context.put(OdooExternalIdReferenceProvider.FILTER, OdooRecordFilters.RES_GROUPS);
                        context.put(OdooExternalIdReferenceProvider.COMMA_SEPARATED, true);
                        return true;
                    }
                    return false;
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> RES_CONFIG_FIELD_ATTR_IMPLIED_GROUP_PATTERN =
            OdooModelUtils.getFieldAttributePattern(-1, "implied_group").with(new PatternCondition<PyStringLiteralExpression>("fieldImpliedGroup") {
                @Override
                public boolean accepts(@NotNull PyStringLiteralExpression pyStringLiteralExpression,
                                       ProcessingContext context) {
                    OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(pyStringLiteralExpression);
                    if (modelClass != null && OdooNames.RES_CONFIG_SETTINGS.equals(modelClass.getName())) {
                        context.put(OdooExternalIdReferenceProvider.FILTER, OdooRecordFilters.RES_GROUPS);
                        context.put(OdooExternalIdReferenceProvider.COMMA_SEPARATED, true);
                        return true;
                    }
                    return false;
                }
            });

    public static final PsiElementPattern.Capture<PyStringLiteralExpression> USER_HAS_GROUP_PATTERN =
            psiElement(PyStringLiteralExpression.class).afterLeafSkipping(psiElement(PyTokenTypes.LPAR),
                    psiElement(PsiElement.class).with(new PatternCondition<PsiElement>("hasGroup") {
                        @Override
                        public boolean accepts(@NotNull PsiElement psiElement,
                                               ProcessingContext context) {
                            String text = psiElement.getText();
                            if (text.equals("has_group") || text.equals("user_has_groups")) {
                                context.put(OdooExternalIdReferenceProvider.FILTER, OdooRecordFilters.RES_GROUPS);
                                return true;
                            }
                            return false;
                        }
                    }));

    public static final XmlAttributeValuePattern T_PATTERN =
            XmlPatterns.xmlAttributeValue("t-call", "t-call-assets", "t-snippet")
                    .with(OdooXmlUtils.ODOO_XML_DATA_ELEMENT_PATTERN_CONDITION)
                    .with(new PatternCondition<XmlAttributeValue>("tCall") {
                        @Override
                        public boolean accepts(@NotNull XmlAttributeValue xmlAttributeValue,
                                               ProcessingContext context) {
                            if (xmlAttributeValue.getValue().contains("{")) {
                                return false;
                            }
                            context.put(OdooExternalIdReferenceProvider.FILTER, OdooRecordFilters.QWEB);
                            return true;
                        }
                    });

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        OdooExternalIdReferenceProvider provider = new OdooExternalIdReferenceProvider();
        registrar.registerReferenceProvider(REF_PATTERN, provider);
        registrar.registerReferenceProvider(REQUEST_RENDER_PATTERN, provider);
        registrar.registerReferenceProvider(FIELD_ATTR_GROUPS_PATTERN, provider);
        registrar.registerReferenceProvider(RES_CONFIG_FIELD_ATTR_GROUP_PATTERN, provider);
        registrar.registerReferenceProvider(RES_CONFIG_FIELD_ATTR_IMPLIED_GROUP_PATTERN, provider);
        registrar.registerReferenceProvider(USER_HAS_GROUP_PATTERN, provider);
        registrar.registerReferenceProvider(T_PATTERN, provider);
    }
}
