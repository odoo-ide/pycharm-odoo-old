package dev.ngocta.pycharm.odoo.python.psi;

import com.intellij.lang.ASTNode;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.impl.PySliceExpressionImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooPySliceExpression extends PySliceExpressionImpl {
    public OdooPySliceExpression(ASTNode astNode) {
        super(astNode);
    }

    @Nullable
    @Override
    public PyType getType(@NotNull TypeEvalContext context,
                          @NotNull TypeEvalContext.Key key) {
        PyExpression operand = getOperand();
        PyType operandType = context.getType(operand);
        if (operandType instanceof OdooModelClassType) {
            return operandType;
        }
        return super.getType(context, key);
    }
}
