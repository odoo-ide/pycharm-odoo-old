package dev.ngocta.pycharm.odoo.model;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.intellij.codeInsight.completion.BasicInsertHandler;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericValue;
import com.jetbrains.python.codeInsight.completion.PyFunctionInsertHandler;
import com.jetbrains.python.codeInsight.mlcompletion.PyCompletionMlElementInfo;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyCallableParameter;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooPyUtils;
import dev.ngocta.pycharm.odoo.data.*;
import dev.ngocta.pycharm.odoo.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooModelUtils {
    private static final double COMPLETION_PRIORITY_FIELD = 2;
    private static final double COMPLETION_PRIORITY_FUNCTION = 1;
    private static final Set<String> KNOWN_FIELD_TYPES = ImmutableSet.copyOf(OdooNames.FIELD_TYPES);
    private static final ImmutableMap<String, String> KNOWN_FIELD_DOMAIN_TO_FIELD_MODEL = ImmutableMap.<String, String>builder()
            .put(OdooNames.IR_RULE_DOMAIN_FORCE, OdooNames.IR_RULE_MODEL_ID)
            .put(OdooNames.IR_ACTIONS_ACT_WINDOW_DOMAIN, OdooNames.IR_ACTIONS_ACT_WINDOW_RES_MODEL)
            .build();

    private OdooModelUtils() {
    }

    public static OdooModelClass getContainingOdooModelClass(@NotNull PsiElement element) {
        PyClass cls = PyUtil.getContainingClassOrSelf(element);
        if (cls != null) {
            OdooModelInfo info = OdooModelInfo.getInfo(cls);
            if (info != null) {
                return OdooModelClass.getInstance(info.getName(), element.getProject());
            }
        }
        return null;
    }

    @Nullable
    public static LookupElement createCompletionLine(@NotNull PsiNamedElement element, @NotNull TypeEvalContext context) {
        String name = element.getName();
        if (name == null) {
            return null;
        }
        String tailText = null;
        String typeText = null;
        double priority = 0;
        InsertHandler<LookupElement> insertHandler = new BasicInsertHandler<>();
        if (element instanceof PyTargetExpression) {
            OdooFieldInfo info = OdooFieldInfo.getInfo((PyTargetExpression) element);
            if (info != null) {
                typeText = info.getTypeName();
                PyType type = info.getType(context);
                if (type instanceof OdooModelClassType) {
                    typeText = "(" + type.getName() + ") " + typeText;
                }
                priority = COMPLETION_PRIORITY_FIELD;
            }
        } else if (element instanceof PyFunction && ((PyFunction) element).getProperty() == null) {
            List<PyCallableParameter> params = ((PyFunction) element).getParameters(context);
            String paramsText = StringUtil.join(params, PyCallableParameter::getName, ", ");
            tailText = "(" + paramsText + ")";
            priority = COMPLETION_PRIORITY_FUNCTION;
            insertHandler = PyFunctionInsertHandler.INSTANCE;
        }
        LookupElement lookupElementBuilder = LookupElementBuilder.create(element)
                .withTailText(tailText)
                .withTypeText(typeText)
                .withIcon(element.getIcon(Iconable.ICON_FLAG_READ_STATUS))
                .withInsertHandler(insertHandler);
        LookupElement lookupElement = PrioritizedLookupElement.withPriority(lookupElementBuilder, priority);
        PyCompletionMlElementInfo info = PyCompletionMlElementInfo.Companion.fromElement(element);
        lookupElement.putUserData(PyCompletionMlElementInfo.Companion.getKey(), info);
        return lookupElement;
    }

    @NotNull
    public static PsiElementPattern.Capture<PyStringLiteralExpression> getFieldArgumentPattern(int index, @NotNull String keyword, String... fieldType) {
        return psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("fieldArgument") {
            @Override
            public boolean accepts(@NotNull PyStringLiteralExpression stringExpression, ProcessingContext context) {
                PsiElement parent = stringExpression.getParent();
                if (parent instanceof PyArgumentList || parent instanceof PyKeywordArgument) {
                    PyCallExpression callExpression = PsiTreeUtil.getParentOfType(parent, PyCallExpression.class);
                    if (callExpression != null && isFieldDeclarationExpression(callExpression)) {
                        PyExpression callee = callExpression.getCallee();
                        if (callee instanceof PyReferenceExpression) {
                            String calleeName = callee.getName();
                            if (calleeName != null && (fieldType.length == 0 || Arrays.asList(fieldType).contains(calleeName))) {
                                PyStringLiteralExpression argExpression;
                                if (index >= 0) {
                                    argExpression = callExpression.getArgument(index, keyword, PyStringLiteralExpression.class);
                                } else {
                                    argExpression = ObjectUtils.tryCast(callExpression.getKeywordArgument(keyword), PyStringLiteralExpression.class);
                                }
                                return stringExpression.equals(argExpression);
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    public static boolean isFieldDeclarationExpression(@NotNull PyCallExpression callExpression) {
        PyExpression callee = callExpression.getCallee();
        if (callee instanceof PyReferenceExpression) {
            String calleeName = callee.getName();
            if (KNOWN_FIELD_TYPES.contains(calleeName)) {
                return true;
            }
            PsiReference ref = callee.getReference();
            if (ref != null) {
                PsiElement target = ref.resolve();
                PyClass targetClass = null;
                if (target instanceof PyClass) {
                    targetClass = (PyClass) target;
                } else if (target instanceof PyFunction && PyUtil.isInitMethod(target)) {
                    targetClass = ((PyFunction) target).getContainingClass();
                }
                if (targetClass != null) {
                    return targetClass.isSubclass(OdooNames.FIELD_QNAME, null);
                }
            }
        }
        return false;
    }

    public static PyClass getBaseModelClass(@Nullable PsiElement anchor) {
        if (anchor != null) {
            return OdooPyUtils.getClassByQName(OdooNames.BASE_MODEL_QNAME, anchor);
        }
        return null;
    }

    public static boolean isOdooModelFile(@Nullable PsiFile file) {
        return file instanceof PyFile && OdooModuleUtils.isInOdooModule(file);
    }

    @Nullable
    public static OdooModelClass getSearchDomainModelContext(@NotNull PsiElement leftOrRightOperand) {
        Project project = leftOrRightOperand.getProject();
        PsiElement parent = leftOrRightOperand.getParent();
        if (!(parent instanceof PyTupleExpression
                || parent instanceof PyListLiteralExpression
                || parent instanceof PyParenthesizedExpression)) {
            return null;
        }
        PsiElement[] sequenceElements;
        if (parent instanceof PySequenceExpression) {
            sequenceElements = ((PySequenceExpression) parent).getElements();
        } else {
            sequenceElements = new PsiElement[]{leftOrRightOperand};
        }
        boolean isLeft = leftOrRightOperand instanceof PyStringLiteralExpression
                && sequenceElements.length > 0
                && sequenceElements[0].equals(leftOrRightOperand);
        boolean isRight = leftOrRightOperand instanceof PyReferenceExpression
                && sequenceElements.length > 2
                && sequenceElements[0] instanceof PyLiteralExpression
                && sequenceElements[1] instanceof PyLiteralExpression
                && sequenceElements[2].equals(leftOrRightOperand);
        if (!isLeft && !isRight) {
            return null;
        }
        parent = parent.getParent();
        if (parent instanceof PyParenthesizedExpression) {
            parent = parent.getParent();
        }
        if (!(parent instanceof PyListLiteralExpression)) {
            return null;
        }
        parent = parent.getParent();
        if (parent instanceof PyKeywordArgument) {
            parent = parent.getParent();
        }
        if (parent == null) {
            return null;
        }
        if (parent instanceof PyArgumentList) {
            parent = parent.getParent();
            if (parent instanceof PyCallExpression) {
                PyExpression callee = ((PyCallExpression) parent).getCallee();
                if (callee instanceof PyReferenceExpression) {
                    PyReferenceExpression ref = (PyReferenceExpression) callee;
                    String refName = ref.getName();
                    PyExpression qualifier = ref.getQualifier();
                    if (qualifier != null && (
                            OdooNames.SEARCH.equals(refName)
                                    || OdooNames.SEARCH_READ.equals(refName)
                                    || OdooNames.SEARCH_COUNT.equals(refName))) {
                        TypeEvalContext context = TypeEvalContext.userInitiated(project, parent.getContainingFile());
                        PyType type = context.getType(qualifier);
                        if (type instanceof OdooModelClassType) {
                            return ((OdooModelClassType) type).getPyClass();
                        }
                    }
                }
            }
            return null;
        }
        if (parent instanceof PyKeyValueExpression) {
            parent = parent.getParent();
            if (parent instanceof PyDictLiteralExpression) {
                parent = parent.getParent();
            }
        }
        parent = parent.getParent();
        if (!(parent instanceof PyFile)) {
            return null;
        }
        parent = parent.getContext();
        if (parent instanceof PyStringLiteralExpression) {
            parent = parent.getParent();
            if (parent instanceof PyKeywordArgument) {
                if (OdooNames.FIELD_ATTR_DOMAIN.equals(((PyKeywordArgument) parent).getKeyword())) {
                    if (isLeft) {
                        parent = PsiTreeUtil.getParentOfType(parent, PyAssignmentStatement.class);
                        if (parent != null) {
                            PyExpression left = ((PyAssignmentStatement) parent).getLeftHandSideExpression();
                            if (left instanceof PyTargetExpression) {
                                OdooFieldInfo info = OdooFieldInfo.getInfo((PyTargetExpression) left);
                                if (info != null && info.getComodel() != null) {
                                    return OdooModelClass.getInstance(info.getComodel(), project);
                                }
                            }
                        }
                    } else {
                        return OdooModelUtils.getContainingOdooModelClass(parent);
                    }
                }
            }
            return null;
        }
        DomManager domManager = DomManager.getDomManager(project);
        if (parent instanceof XmlAttributeValue) {
            parent = parent.getParent();
            if (parent instanceof XmlAttribute) {
                XmlAttribute attribute = (XmlAttribute) parent;
                XmlTag tag = attribute.getParent();
                if (tag != null) {
                    DomElement domElement = domManager.getDomElement(tag);
                    if (domElement instanceof OdooDomModelScopedViewElement) {
                        String model;
                        if (leftOrRightOperand instanceof OdooDomViewField
                                && OdooNames.FIELD_ATTR_DOMAIN.equals(attribute.getName())
                                && isLeft) {
                            model = ((OdooDomField) domElement).getComodel();
                        } else {
                            model = ((OdooDomModelScopedViewElement) domElement).getModel();
                        }
                        if (model != null) {
                            return OdooModelClass.getInstance(model, project);
                        }
                    }
                }
            }
            return null;
        }
        if (parent instanceof XmlText) {
            parent = parent.getParent();
            if (parent instanceof XmlTag) {
                XmlTag tag = (XmlTag) parent;
                DomElement domElement = domManager.getDomElement(tag);
                if (domElement instanceof OdooDomFieldAssignment) {
                    String field = Optional.of((OdooDomFieldAssignment) domElement)
                            .map(OdooDomField::getName)
                            .map(GenericValue::getStringValue)
                            .orElse(null);
                    if (field == null) {
                        return null;
                    }
                    String modelField = KNOWN_FIELD_DOMAIN_TO_FIELD_MODEL.getOrDefault(field, null);
                    if (modelField == null) {
                        return null;
                    }
                    OdooDomRecord record = domElement.getParentOfType(OdooDomRecord.class, true);
                    if (record != null) {
                        for (OdooDomFieldAssignment f : record.getFields()) {
                            if (modelField.equals(f.getName().getStringValue())) {
                                XmlAttributeValue refValue = f.getRef().getXmlAttributeValue();
                                if (refValue != null) {
                                    return Optional.of(refValue)
                                            .map(PsiElement::getReference)
                                            .map(PsiReference::resolve)
                                            .map(OdooModelUtils::getContainingOdooModelClass)
                                            .orElse(null);
                                }
                                String model = f.getValue();
                                if (model != null) {
                                    return OdooModelClass.getInstance(model, project);
                                }
                                return null;
                            }
                        }
                    }
                    return null;
                }
                if (domElement instanceof OdooDomModelScopedViewElement) {
                    String model = ((OdooDomModelScopedViewElement) domElement).getModel();
                    if (model != null) {
                        return OdooModelClass.getInstance(model, project);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    public static OdooModelClass getRecordValueModelContext(@NotNull PyStringLiteralExpression field) {
        Project project = field.getProject();
        PsiElement parent = field.getParent();
        if (parent instanceof PyKeyValueExpression && ((PyKeyValueExpression) parent).getKey() == field) {
            parent = parent.getParent();
            if (!(parent instanceof PyDictLiteralExpression)) {
                return null;
            }
        } else if (!(parent instanceof PySetLiteralExpression || parent instanceof PyDictLiteralExpression)) {
            return null;
        }
        PsiElement dict = parent;
        parent = parent.getParent();
        if (parent instanceof PyTupleExpression) {
            PsiElement[] tupleElements = ((PyTupleExpression) parent).getElements();
            if (tupleElements.length == 3 && tupleElements[0] instanceof PyNumericLiteralExpression && tupleElements[2].equals(dict)) {
                parent = parent.getParent();
                if (parent instanceof PyParenthesizedExpression) {
                    parent = parent.getParent();
                    if (parent instanceof PyListLiteralExpression) {
                        parent = parent.getParent();
                        if (parent instanceof PyKeyValueExpression) {
                            PyExpression k = ((PyKeyValueExpression) parent).getKey();
                            if (k instanceof PyStringLiteralExpression) {
                                PsiElement ref = Optional.ofNullable(k.getReference())
                                        .map(PsiReference::resolve)
                                        .orElse(null);
                                if (ref instanceof PyTargetExpression) {
                                    OdooFieldInfo info = OdooFieldInfo.getInfo((PyTargetExpression) ref);
                                    if (info != null && info.getComodel() != null) {
                                        return OdooModelClass.getInstance(info.getComodel(), project);
                                    }
                                }
                            }
                        } else {
                            parent = parent.getParent();
                            if (parent instanceof PyFile) {
                                parent = parent.getContext();
                                if (parent instanceof XmlAttributeValue) {
                                    parent = parent.getParent();
                                    if (parent instanceof XmlAttribute) {
                                        XmlAttribute attribute = (XmlAttribute) parent;
                                        if ("eval".equals(attribute.getName())) {
                                            XmlTag tag = attribute.getParent();
                                            if (tag != null) {
                                                DomManager domManager = DomManager.getDomManager(project);
                                                DomElement domElement = domManager.getDomElement(tag);
                                                if (domElement instanceof OdooDomFieldAssignment) {
                                                    String comodel = ((OdooDomFieldAssignment) domElement).getComodel();
                                                    if (comodel != null) {
                                                        return OdooModelClass.getInstance(comodel, project);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return null;
        }
        if (parent instanceof PyListLiteralExpression || parent instanceof PyListCompExpression) {
            parent = parent.getParent();
        }
        if (parent instanceof PyArgumentList) {
            parent = parent.getParent();
            if (parent instanceof PyCallExpression) {
                PyExpression callee = ((PyCallExpression) parent).getCallee();
                if (callee instanceof PyReferenceExpression) {
                    PyReferenceExpression ref = (PyReferenceExpression) callee;
                    String refName = ref.getName();
                    PyExpression qualifier = ref.getQualifier();
                    if (qualifier != null && (
                            OdooNames.CREATE.equals(refName)
                                    || OdooNames.WRITE.equals(refName)
                                    || OdooNames.UPDATE.equals(refName))) {
                        TypeEvalContext typeEvalContext = TypeEvalContext.userInitiated(project, parent.getContainingFile());
                        PyType type = typeEvalContext.getType(qualifier);
                        if (type instanceof OdooModelClassType) {
                            return ((OdooModelClassType) type).getPyClass();
                        }
                    }
                }
            }
        }
        return null;
    }
}
