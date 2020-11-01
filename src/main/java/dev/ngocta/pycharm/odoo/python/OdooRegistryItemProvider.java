package dev.ngocta.pycharm.odoo.python;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyStringLiteralExpressionImpl;
import com.jetbrains.python.psi.types.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.python.model.OdooRecordSetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooRegistryItemProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getCallType(@NotNull PyFunction function,
                                   @NotNull PyCallSiteExpression callSite,
                                   @NotNull TypeEvalContext context) {
        String functionName = function.getName();
        if (PyNames.GETITEM.equals(functionName) && callSite instanceof PySubscriptionExpression) {
            PySubscriptionExpression subscription = (PySubscriptionExpression) callSite;
            Project project = subscription.getProject();
            PyExpression operand = subscription.getOperand();
            if (OdooPyUtils.isRegistryTypeExpression(operand, context)) {
                PyExpression index = subscription.getIndexExpression();
                if (index instanceof PyLiteralExpression) {
                    String model = ((PyStringLiteralExpressionImpl) index).getStringValue();
                    OdooModelClassType modelClassType = new OdooModelClassType(model, OdooRecordSetType.NONE, project);
                    return Ref.create(modelClassType);
                } else {
                    PyClassType type = OdooPyUtils.getClassTypeByQName(OdooNames.BASE_MODEL_CLASS_QNAME, function, true);
                    return Ref.create(PyUnionType.createWeakType(type));
                }
            }
        }
        return null;
    }
}
