package dev.ngocta.pycharm.odoo.python;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.codeInsight.imports.AutoImportQuickFix;
import com.jetbrains.python.codeInsight.imports.PyImportCandidateProvider;
import com.jetbrains.python.psi.resolve.PyResolveImportUtil;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;

public class OdooImportCandidateProvider implements PyImportCandidateProvider {
    @Override
    public void addImportCandidates(PsiReference psiReference,
                                    String s,
                                    AutoImportQuickFix autoImportQuickFix) {
        PsiElement element = psiReference.getElement();
        if (!OdooModuleUtils.isInOdooModule(element)) {
            return;
        }
        if ("_".equals(s)) {
            QualifiedName qualifiedName = QualifiedName.fromDottedString("odoo.tools.translate._");
            PsiElement importElement = PyResolveImportUtil.resolveTopLevelMember(qualifiedName, PyResolveImportUtil.fromFoothold(element));
            if (importElement == null) {
                return;
            }
            autoImportQuickFix.addImport(importElement, importElement.getContainingFile(), QualifiedName.fromComponents("odoo"), null);
        }
    }
}
