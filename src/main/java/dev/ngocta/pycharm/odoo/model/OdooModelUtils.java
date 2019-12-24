package dev.ngocta.pycharm.odoo.model;

import com.google.common.collect.ImmutableSet;
import com.intellij.codeInsight.completion.BasicInsertHandler;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.codeInsight.completion.PyFunctionInsertHandler;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyCallableParameter;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooModelUtils {
    private static final double COMPLETION_PRIORITY_FIELD = 2;
    private static final double COMPLETION_PRIORITY_FUNCTION = 1;
    private static final Set<String> KNOWN_FIELD_TYPES = ImmutableSet.of(
            OdooNames.FIELD_TYPE_ID,
            OdooNames.FIELD_TYPE_MANY2ONE,
            OdooNames.FIELD_TYPE_ONE2MANY,
            OdooNames.FIELD_TYPE_MANY2MANY,
            OdooNames.FIELD_TYPE_INTEGER,
            OdooNames.FIELD_TYPE_FLOAT,
            OdooNames.FIELD_TYPE_BOOLEAN,
            OdooNames.FIELD_TYPE_INTEGER,
            OdooNames.FIELD_TYPE_FLOAT,
            OdooNames.FIELD_TYPE_MONETARY,
            OdooNames.FIELD_TYPE_CHAR,
            OdooNames.FIELD_TYPE_TEXT,
            OdooNames.FIELD_TYPE_HTML,
            OdooNames.FIELD_TYPE_SELECTION,
            OdooNames.FIELD_TYPE_DATE,
            OdooNames.FIELD_TYPE_DATETIME,
            OdooNames.FIELD_TYPE_BINARY
    );

    private OdooModelUtils() {
    }

    public static OdooModelClass getContainingOdooModelClass(@NotNull PsiElement element) {
        PyClass cls = PyUtil.getContainingClassOrSelf(element);
        if (cls != null) {
            OdooModelInfo info = OdooModelInfo.getInfo(cls);
            if (info != null) {
                return OdooModelClass.create(info.getName(), element.getProject());
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
        } else if (element instanceof PyFunction) {
            List<PyCallableParameter> params = ((PyFunction) element).getParameters(context);
            String paramsText = StringUtil.join(params, PyCallableParameter::getName, ", ");
            tailText = "(" + paramsText + ")";
            priority = COMPLETION_PRIORITY_FUNCTION;
            insertHandler = PyFunctionInsertHandler.INSTANCE;
        }
        LookupElement lookupElement = LookupElementBuilder.create(element)
                .withTailText(tailText)
                .withTypeText(typeText)
                .withIcon(element.getIcon(Iconable.ICON_FLAG_READ_STATUS))
                .withInsertHandler(insertHandler);
        return PrioritizedLookupElement.withPriority(lookupElement, priority);
    }

    public static PsiElementPattern.Capture<PyStringLiteralExpression> getFieldArgumentPattern(int index, String keyword, String... fieldType) {
        return psiElement(PyStringLiteralExpression.class).with(new PatternCondition<PyStringLiteralExpression>("fieldArgument") {
            @Override
            public boolean accepts(@NotNull PyStringLiteralExpression stringExpression, ProcessingContext context) {
                PsiElement parent = stringExpression.getParent();
                if (parent instanceof PyArgumentList || parent instanceof PyKeywordArgument) {
                    PyCallExpression callExpression = PsiTreeUtil.getParentOfType(parent, PyCallExpression.class);
                    if (callExpression != null) {
                        PyExpression callee = callExpression.getCallee();
                        if (callee instanceof PyReferenceExpression) {
                            String calleeName = callee.getName();
                            if (calleeName != null && Arrays.asList(fieldType).contains(calleeName)) {
                                PyStringLiteralExpression comodelExpression = callExpression.getArgument(index, keyword, PyStringLiteralExpression.class);
                                return stringExpression.equals(comodelExpression);
                            }
                        }
                    }
                }
                return false;
            }
        });
    }

    public static boolean isFieldCallExpression(@com.sun.istack.NotNull PyCallExpression callExpression) {
        PyExpression callee = callExpression.getCallee();
        if (callee instanceof PyReferenceExpression) {
            String calleeName = callee.getName();
            return KNOWN_FIELD_TYPES.contains(calleeName);
        }
        return false;
    }
}
