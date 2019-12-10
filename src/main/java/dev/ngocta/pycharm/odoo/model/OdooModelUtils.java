package dev.ngocta.pycharm.odoo.model;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyUtil;
import com.sun.istack.NotNull;

public class OdooModelUtils {
    private OdooModelUtils() {
    }

    public static OdooModelClass getContainingOdooModelClass(@NotNull PsiElement element) {
        PyClass cls = PyUtil.getContainingClassOrSelf(element);
        if (cls != null) {
            OdooModelInfo info = OdooModelInfo.getInfo(cls);
            if (info != null) {
                return OdooModelClass.create(info.getName(), element.getProject());
            }
        }
        return null;
    }
}
