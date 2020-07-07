package dev.ngocta.pycharm.odoo.python;

import com.intellij.execution.Location;
import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.jetbrains.python.run.RunnableScriptFilter;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooPyRunnableScriptFilter implements RunnableScriptFilter {
    @Override
    public boolean isRunnableScript(PsiFile psiFile,
                                    @NotNull Module module,
                                    Location location,
                                    @Nullable TypeEvalContext typeEvalContext) {
        return OdooModuleUtils.isInOdooModule(psiFile);
    }
}
