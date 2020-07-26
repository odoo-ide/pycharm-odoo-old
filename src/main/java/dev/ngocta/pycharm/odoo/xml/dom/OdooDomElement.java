package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.psi.PsiFile;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomUtil;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;

public interface OdooDomElement extends DomElement {
    default PsiFile getFile() {
        return DomUtil.getFile(this);
    }

    default OdooModule getOdooModule() {
        return OdooModuleUtils.getContainingOdooModule(getFile());
    }
}
