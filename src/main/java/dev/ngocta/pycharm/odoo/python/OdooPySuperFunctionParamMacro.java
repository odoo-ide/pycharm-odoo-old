package dev.ngocta.pycharm.odoo.python;

import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.codeInsight.liveTemplates.PythonTemplateContextType;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyNamedParameter;
import com.jetbrains.python.psi.PyParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class OdooPySuperFunctionParamMacro extends Macro {
    @Override
    public String getName() {
        return "pySuperFunctionParam";
    }

    @Override
    public String getPresentableName() {
        return "pySuperFunctionParam()";
    }

    @Override
    @Nullable
    public Result calculateResult(@NotNull Expression[] params, ExpressionContext context) {
        PsiElement place = context.getPsiElementAtStartOffset();
        PyFunction pyFunction = PsiTreeUtil.getParentOfType(place, PyFunction.class);
        if (pyFunction == null) {
            return null;
        }
        PyParameter[] pyParameters = pyFunction.getParameterList().getParameters();
        List<String> paramNames = new LinkedList<>();
        for (PyParameter pyParameter : pyParameters) {
            PyNamedParameter namedParameter = pyParameter.getAsNamed();
            if (namedParameter != null && !namedParameter.isSelf()) {
                paramNames.add(namedParameter.getRepr(false));
            }
        }
        String text = String.join(", ", paramNames);
        return new TextResult(text);
    }

    @Override
    public boolean isAcceptableInContext(TemplateContextType context) {
        return context instanceof PythonTemplateContextType;
    }
}
