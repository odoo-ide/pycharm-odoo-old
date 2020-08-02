package dev.ngocta.pycharm.odoo.data;

import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageViewShortNameLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooRecordElementDescriptionProvider implements ElementDescriptionProvider {
    @Override
    @Nullable
    public String getElementDescription(@NotNull PsiElement element,
                                        @NotNull ElementDescriptionLocation location) {
        if (element instanceof OdooRecordElement && location instanceof UsageViewShortNameLocation) {
            return ((OdooRecordElement) element).getRecord().getUnqualifiedId();
        }
        return null;
    }
}
