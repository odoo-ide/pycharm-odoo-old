package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.ObjectUtils;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OdooModuleHookFunctionReference extends PsiReferenceBase<PsiElement> implements PsiReference {
    public OdooModuleHookFunctionReference(@NotNull PsiElement element) {
        super(element);
    }

    @Override
    @Nullable
    public PsiElement resolve() {
        return PyUtil.getNullableParameterizedCachedValue(getElement(), null, param -> {
            PyFile initFile = getInitFile();
            if (initFile == null) {
                return null;
            }
            List<RatedResolveResult> resolveResults = initFile.multiResolveName(getValue(), true);
            for (RatedResolveResult resolveResult : resolveResults) {
                return resolveResult.getElement();
            }
            return null;
        });
    }

    @Override
    public Object @NotNull [] getVariants() {
        PyFile file = getInitFile();
        if (file != null) {
            return file.getTopLevelFunctions().toArray();
        }
        return new Object[0];
    }

    @Nullable
    private PyFile getInitFile() {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(getElement());
        if (module == null) {
            return null;
        }
        PsiFile file = module.getDirectory().findFile(PyNames.INIT_DOT_PY);
        return ObjectUtils.tryCast(file, PyFile.class);
    }
}
