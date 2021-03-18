package dev.ngocta.pycharm.odoo.python;

import com.intellij.codeInsight.template.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.codeInsight.liveTemplates.PythonTemplateContextType;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooPyFunctionSelfParamNameMacro extends Macro {
    @Override
    public String getName() {
        return "pyFunctionSelfParamName";
    }

    @Override
    public String getPresentableName() {
        return "pyFunctionSelfParamName()";
    }

    @Override
    @Nullable
    public Result calculateResult(Expression @NotNull [] params, ExpressionContext context) {
        String selfName = "self";
        PsiElement place = context.getPsiElementAtStartOffset();
        PyFunction pyFunction = PsiTreeUtil.getParentOfType(place, PyFunction.class);
        if (pyFunction != null) {
            PyParameter[] pyParameters = pyFunction.getParameterList().getParameters();
            if (pyParameters.length > 0 && pyParameters[0].isSelf()) {
                String name = pyParameters[0].getName();
                if (name != null) {
                    selfName = name;
                }
            }
        }
        return new TextResult(selfName);
    }

    @Override
    public boolean isAcceptableInContext(TemplateContextType context) {
        return context instanceof PythonTemplateContextType;
    }
}
