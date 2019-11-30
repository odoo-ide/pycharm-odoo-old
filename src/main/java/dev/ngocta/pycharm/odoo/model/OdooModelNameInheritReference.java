package dev.ngocta.pycharm.odoo.model;

import com.intellij.codeInsight.completion.CompletionUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.jetbrains.python.psi.PyClass;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class OdooModelNameInheritReference extends OdooModelNameReference {
    public OdooModelNameInheritReference(@NotNull PsiElement element) {
        super(element);
    }

    @Override
    protected @NotNull List<PyClass> findModelClasses() {
        List<PyClass> result = super.findModelClasses();
        PyClass cls = PsiTreeUtil.getParentOfType(getElement(), PyClass.class);
        if (cls != null) {
            result.remove(cls);
        }
        return result;
    }

    @NotNull
    @Override
    protected Set<String> getVariantsInner() {
        Set<String> result = super.getVariantsInner();
        String realValue = getValue().replace(CompletionUtilCore.DUMMY_IDENTIFIER, "");
        result.remove(realValue);
        return result;
    }
}
