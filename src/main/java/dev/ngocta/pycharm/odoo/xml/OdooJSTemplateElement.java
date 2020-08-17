package dev.ngocta.pycharm.odoo.xml;

import com.intellij.psi.PsiTarget;
import com.intellij.psi.impl.PomTargetPsiElementImpl;
import com.intellij.util.PlatformIcons;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class OdooJSTemplateElement extends PomTargetPsiElementImpl {
    public OdooJSTemplateElement(@NotNull PsiTarget target) {
        super(target);
    }

    @Override
    public String getLocationString() {
        return OdooModuleUtils.getLocationStringForFile(getContainingFile());
    }

    @Override
    public Icon getIcon() {
        return PlatformIcons.XML_TAG_ICON;
    }
}
