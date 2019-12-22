package dev.ngocta.pycharm.odoo.model;

import com.intellij.codeInsight.completion.BasicInsertHandler;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.jetbrains.python.codeInsight.completion.PyFunctionInsertHandler;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.types.PyCallableParameter;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.sun.istack.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooModelUtils {
    private static final double COMPLETION_PRIORITY_FIELD = 2;
    private static final double COMPLETION_PRIORITY_FUNCTION = 1;

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
}
