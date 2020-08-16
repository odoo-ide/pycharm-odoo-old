package dev.ngocta.pycharm.odoo.python.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyNumericLiteralExpression;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.impl.PySubscriptionExpressionImpl;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.model.OdooFieldInfo;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooPySubscriptionExpression extends PySubscriptionExpressionImpl {
    public OdooPySubscriptionExpression(ASTNode astNode) {
        super(astNode);
    }

    @Nullable
    @Override
    public PyType getType(@NotNull TypeEvalContext context,
                          @NotNull TypeEvalContext.Key key) {
        PyExpression operand = getOperand();
        PyType operandType = context.getType(operand);
        if (operandType instanceof OdooModelClassType) {
            OdooModelClassType modelClassType = (OdooModelClassType) operandType;
            PyExpression index = getIndexExpression();
            if (index instanceof PyStringLiteralExpression) {
                String fieldName = ((PyStringLiteralExpression) index).getStringValue();
                PsiElement field = modelClassType.resolvePsiMember(fieldName, context);
                return OdooFieldInfo.getFieldType(field, context);
            }
            if (index instanceof PyNumericLiteralExpression) {
                return modelClassType.withOneRecord();
            }
            return null;
        }
        return super.getType(context, key);
    }
}
