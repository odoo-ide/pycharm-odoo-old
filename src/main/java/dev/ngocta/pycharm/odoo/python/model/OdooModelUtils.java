package dev.ngocta.pycharm.odoo.python.model;

import com.google.common.collect.ImmutableMap;
import com.intellij.codeInsight.completion.BasicInsertHandler;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ArrayUtil;
import com.intellij.util.ObjectUtils;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.GenericValue;
import com.jetbrains.python.codeInsight.completion.PyFunctionInsertHandler;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyCallableParameter;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.python.OdooPyUtils;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.dom.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooModelUtils {
    private static final double COMPLETION_PRIORITY_FIELD = 2;
    private static final double COMPLETION_PRIORITY_FUNCTION = 1;
    private static final ImmutableMap<String, String> KNOWN_DOMAIN_FIELDS = ImmutableMap.<String, String>builder()
            .put(OdooNames.IR_RULE_FIELD_DOMAIN_FORCE, OdooNames.IR_RULE_FIELD_MODEL_ID)
            .put(OdooNames.IR_ACTIONS_ACT_WINDOW_FIELD_DOMAIN, OdooNames.IR_ACTIONS_ACT_WINDOW_FIELD_RES_MODEL)
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
    public static LookupElement createCompletionLine(@NotNull PsiNamedElement element,
                                                     @NotNull TypeEvalContext context) {
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
        return PrioritizedLookupElement.withPriority(lookupElementBuilder, priority);
    }

    @NotNull
    public static PsiElementPattern.Capture<PyStringLiteralExpression> getFieldArgumentPattern(int index,
                                                                                               @NotNull String keyword,
                                                                                               String... fieldType) {
        return psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("fieldArgument") {
            @Override
            public boolean accepts(@NotNull PyStringLiteralExpression stringExpression,
                                   ProcessingContext context) {
                PsiElement parent = stringExpression.getParent();
                if (parent instanceof PyArgumentList || parent instanceof PyKeywordArgument) {
                    PyCallExpression callExpression = PsiTreeUtil.getParentOfType(parent, PyCallExpression.class);
                    if (callExpression != null && isKnownFieldDeclarationExpression(callExpression)) {
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

    public static boolean isKnownFieldDeclarationExpression(@NotNull PyCallExpression callExpression) {
        PyExpression callee = callExpression.getCallee();
        if (callee instanceof PyReferenceExpression) {
            String calleeName = callee.getName();
            return ArrayUtil.contains(calleeName, OdooNames.FIELD_TYPES);
        }
        return false;
    }

    public static boolean isFieldDeclarationExpression(@NotNull PyCallExpression callExpression) {
        if (isKnownFieldDeclarationExpression(callExpression)) {
            return true;
        }
        if (DumbService.isDumb(callExpression.getProject())) {
            return false;
        }
        PyExpression callee = callExpression.getCallee();
        if (callee instanceof PyReferenceExpression) {
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
                    return targetClass.isSubclass(OdooNames.FIELD_CLASS_QNAME, null);
                }
            }
        }
        return false;
    }

    @Nullable
    public static PyClass getBaseModelClass(@Nullable PsiElement anchor) {
        return OdooPyUtils.getClassByQName(OdooNames.BASE_MODEL_CLASS_QNAME, anchor);
    }

    @Nullable
    public static PyListLiteralExpression getSearchDomainExpression(@NotNull PsiElement leftOrRightOperand) {
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
        return ObjectUtils.tryCast(parent, PyListLiteralExpression.class);
    }

    @Nullable
    public static OdooModelClass resolveSearchDomainContext(@NotNull PyListLiteralExpression domainExpression,
                                                            boolean forLeftOperand) {
        return PyUtil.getNullableParameterizedCachedValue(domainExpression, forLeftOperand, param -> {
            PsiElement parent = domainExpression.getParent();
            if (parent instanceof PyKeywordArgument) {
                parent = parent.getParent();
            }
            if (parent == null) {
                return null;
            }
            Project project = domainExpression.getProject();
            if (parent instanceof PyArgumentList) {
                parent = parent.getParent();
                if (parent instanceof PyCallExpression) {
                    PyCallExpression callExpression = ((PyCallExpression) parent);
                    PyExpression callee = callExpression.getCallee();
                    if (callee instanceof PyReferenceExpression) {
                        PyReferenceExpression ref = (PyReferenceExpression) callee;
                        String refName = ref.getName();
                        if (ArrayUtil.contains(refName, OdooNames.RELATIONAL_FIELD_TYPES)) {
                            PyStringLiteralExpression comodelExpression = callExpression.getArgument(
                                    0, OdooNames.FIELD_ATTR_COMODEL_NAME, PyStringLiteralExpression.class);
                            if (comodelExpression != null) {
                                return OdooModelClass.getInstance(comodelExpression.getStringValue(), project);
                            }
                            return null;
                        }
                        PyExpression qualifier = ref.getQualifier();
                        if (qualifier != null && ArrayUtil.contains(refName, OdooNames.SEARCH, OdooNames.SEARCH_READ, OdooNames.SEARCH_COUNT)) {
                            TypeEvalContext typeEvalContext = TypeEvalContext.codeAnalysis(project, parent.getContainingFile());
                            PyType type = typeEvalContext.getType(qualifier);
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
                        if (forLeftOperand) {
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
                            if (domElement instanceof OdooDomViewField
                                    && OdooNames.FIELD_ATTR_DOMAIN.equals(attribute.getName())
                                    && forLeftOperand) {
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
                        String modelField = KNOWN_DOMAIN_FIELDS.getOrDefault(field, null);
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
        });
    }

    @Nullable
    public static PsiElement getRecordValueExpression(@NotNull PsiElement field) {
        PsiElement parent = field.getParent();
        if (parent instanceof PyKeyValueExpression && ((PyKeyValueExpression) parent).getKey() == field) {
            parent = parent.getParent();
        }
        if (parent instanceof PySetLiteralExpression || parent instanceof PyDictLiteralExpression) {
            return parent;
        }
        return null;
    }

    @Nullable
    public static OdooModelClass resolveRecordValueContext(@NotNull PsiElement dict) {
        return PyUtil.getNullableParameterizedCachedValue(dict, null, param -> {
            Project project = dict.getProject();
            PsiElement parent = dict.getParent();
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
                        if (qualifier != null && ArrayUtil.contains(refName, OdooNames.CREATE, OdooNames.WRITE, OdooNames.UPDATE)) {
                            TypeEvalContext context = TypeEvalContext.userInitiated(project, parent.getContainingFile());
                            PyType type = context.getType(qualifier);
                            if (type instanceof OdooModelClassType) {
                                return ((OdooModelClassType) type).getPyClass();
                            }
                        }
                    }
                }
            }
            return null;
        });
    }

    public static String getIrModelRecordName(@NotNull String model) {
        return "model_" + model.replace(".", "_");
    }

    @Nullable
    public static OdooModelClassType extractOdooModelClassType(@Nullable PyType type) {
        if (type == null) {
            return null;
        }
        if (type instanceof OdooModelClassType) {
            return (OdooModelClassType) type;
        }
        PyType extractedType = OdooPyUtils.extractCompositedType(type, OdooModelClassType.class::isInstance);
        if (extractedType != null) {
            return (OdooModelClassType) extractedType;
        }
        return null;
    }

    @NotNull
    public static Collection<PyTargetExpression> findFields(@NotNull String name,
                                                            @NotNull Project project,
                                                            @NotNull GlobalSearchScope scope) {
        Collection<PyTargetExpression> attributes = OdooPyUtils.findClassAttributes(name, project, scope);
        List<PyTargetExpression> fields = new LinkedList<>();
        for (PyTargetExpression attribute : attributes) {
            if (OdooFieldInfo.getInfo(attribute) != null) {
                fields.add(attribute);
            }
        }
        return fields;
    }

    @NotNull
    public static Collection<PyTargetExpression> findFields(@NotNull String name,
                                                            @NotNull PsiElement anchor) {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(anchor);
        if (module == null) {
            return Collections.emptyList();
        }
        return findFields(name, anchor.getProject(), module.getSearchScope());
    }

    @Nullable
    public static PyType guessFieldTypeByName(@NotNull String name,
                                              @NotNull PsiElement anchor,
                                              @NotNull TypeEvalContext context) {
        Collection<PyTargetExpression> fields = findFields(name, anchor);
        Set<PyType> types = new HashSet<>();
        for (PyTargetExpression field : fields) {
            OdooFieldInfo info = OdooFieldInfo.getInfo(field);
            if (info != null) {
                PyType type = info.getType(context);
                if (type != null) {
                    types.add(type);
                }
            }
        }
        if (types.size() == 1) {
            return types.iterator().next();
        }
        return null;
    }

    public static boolean isInheritsAssignedValue(PsiElement element) {
        if (element instanceof PyDictLiteralExpression || element instanceof PySetLiteralExpression) {
            element = element.getParent();
            if (element instanceof PyAssignmentStatement) {
                PsiElement left = ((PyAssignmentStatement) element).getLeftHandSideExpression();
                return left instanceof PyTargetExpression
                        && OdooNames.MODEL_INHERITS.equals(((PyTargetExpression) left).getName());
            }
        }
        return false;
    }

    @NotNull
    public static List<PyClass> getUnknownAncestorModelClasses(@NotNull PyClass cls,
                                                               @NotNull TypeEvalContext context) {
        OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(cls);
        if (modelClass == null) {
            return Collections.emptyList();
        }
        List<PyClass> knownAncestors = cls.getAncestorClasses(context);
        List<PyClass> ancestors = new LinkedList<>(modelClass.getAncestorClasses(context));
        ancestors.removeAll(knownAncestors);
        int clsIndex = ancestors.indexOf(cls);
        if (clsIndex >= 0) {
            ancestors = ancestors.subList(clsIndex + 1, ancestors.size());
        }
        return ancestors;
    }
}
