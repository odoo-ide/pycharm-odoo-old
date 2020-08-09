package dev.ngocta.pycharm.odoo.python.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.python.psi.PyStringLiteralUtil;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

public class OdooPyStringLiteralExpression extends PyStringLiteralExpressionImpl {
    public OdooPyStringLiteralExpression(ASTNode astNode) {
        super(astNode);
    }

    @Override
    public PyType getType(@NotNull TypeEvalContext context,
                          @NotNull TypeEvalContext.Key key) {
        if (OdooModuleUtils.isInOdooModule(context.getOrigin())) {
            String text = getText();
            if (!text.isEmpty() && !PyStringLiteralUtil.isBytesPrefix(text.substring(0, 1))) {
                final PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(context.getOrigin() == null ? this : context.getOrigin());
                return builtinCache.getStrType();
            }
        }
        return super.getType(context, key);
    }
}
