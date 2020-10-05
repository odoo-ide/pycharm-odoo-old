package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageViewTypeLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooJSFieldWidgetDescriptionProvider implements ElementDescriptionProvider {
    @Override
    @Nullable
    public String getElementDescription(@NotNull PsiElement element,
                                        @NotNull ElementDescriptionLocation location) {
        if (element instanceof OdooJSFieldWidget) {
            if (location instanceof UsageViewTypeLocation) {
                return "widget";
            }
        }
        return null;
    }
}
