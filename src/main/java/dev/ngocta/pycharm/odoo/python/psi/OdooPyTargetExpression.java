package dev.ngocta.pycharm.odoo.python.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import com.jetbrains.python.psi.impl.PyTargetExpressionImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import org.jetbrains.annotations.NotNull;

public class OdooPyTargetExpression extends PyTargetExpressionImpl {
    public OdooPyTargetExpression(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public PyType getType(@NotNull TypeEvalContext context,
                          TypeEvalContext.@NotNull Key key) {
        PyType type = super.getType(context, key);
        if (PyPsiUtils.isMethodContext(this)) {
            return OdooModelUtils.upgradeModelType(type);
        }
        return type;
    }
}
