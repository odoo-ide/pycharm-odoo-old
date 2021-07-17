package dev.ngocta.pycharm.odoo.python.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.python.psi.impl.PyNamedParameterImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooPyNamedParameter extends PyNamedParameterImpl {
    public OdooPyNamedParameter(@NotNull ASTNode node) {
        super(node);
    }

    @Override
    public @Nullable PyType getType(@NotNull TypeEvalContext context,
                                    TypeEvalContext.@NotNull Key key) {
        PyType type = super.getType(context, key);
        type = OdooModelUtils.upgradeModelType(type);
        return type;
    }
}
