package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiFile;
import com.intellij.util.PlatformIcons;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import com.intellij.util.xml.ElementPresentation;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface OdooDomElement extends DomElement {
    default PsiFile getFile() {
        return DomUtil.getFile(this);
    }

    default OdooModule getOdooModule() {
        return OdooModuleUtils.getContainingOdooModule(getFile());
    }

    @Override
    @NotNull
    default ElementPresentation getPresentation() {
        return new ElementPresentation() {
            @Override
            public String getElementName() {
                return "";
            }

            @Override
            public String getTypeName() {
                return "";
            }

            @Override
            @Nullable
            public Icon getIcon() {
                return PlatformIcons.XML_TAG_ICON;
            }
        };
    }
}
