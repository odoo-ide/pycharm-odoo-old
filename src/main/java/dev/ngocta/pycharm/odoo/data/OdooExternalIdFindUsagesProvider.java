package dev.ngocta.pycharm.odoo.data;

import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.xml.DomTarget;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRecordLike;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooExternalIdFindUsagesProvider implements FindUsagesProvider {
    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        if (psiElement instanceof OdooRecordElement) {
            return true;
        }
        if (psiElement instanceof PomTargetPsiElement) {
            PomTarget target = ((PomTargetPsiElement) psiElement).getTarget();
            if (target instanceof DomTarget) {
                return ((DomTarget) target).getDomElement() instanceof OdooDomRecordLike;
            }
        }
        return false;
    }

    @Override
    @Nullable
    public String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    @Nls
    @NotNull
    public String getType(@NotNull PsiElement element) {
        return "Record";
    }

    @Override
    @Nls
    @NotNull
    public String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof OdooRecordElement) {
            return ((OdooRecordElement) element).getRecord().getUnqualifiedId();
        }
        return "";
    }

    @Override
    @Nls
    @NotNull
    public String getNodeText(@NotNull PsiElement element,
                              boolean useFullName) {
        return "";
    }
}
