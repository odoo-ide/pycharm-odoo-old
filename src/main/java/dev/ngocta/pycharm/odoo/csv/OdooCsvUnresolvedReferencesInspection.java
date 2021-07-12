package dev.ngocta.pycharm.odoo.csv;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdReference;
import org.jetbrains.annotations.NotNull;

public class OdooCsvUnresolvedReferencesInspection extends LocalInspectionTool {
    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder,
                                          boolean isOnTheFly) {
        return new PsiElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                if (!(element instanceof OdooCsvField)) {
                    return;
                }
                if (OdooCsvUnresolvedReferencesInspection.this.isSuppressedFor(element)) {
                    return;
                }
                PsiReference reference = element.getReference();
                if (reference instanceof OdooExternalIdReference) {
                    if (reference.getCanonicalText().isEmpty()) {
                        return;
                    }
                    if (((OdooExternalIdReference) reference).multiResolve(false).length == 0) {
                        holder.registerProblem(reference);
                    }
                }
            }
        };
    }
}
