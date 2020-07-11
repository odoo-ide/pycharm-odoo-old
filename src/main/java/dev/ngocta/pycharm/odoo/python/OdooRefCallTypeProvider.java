package dev.ngocta.pycharm.odoo.python;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyCallSiteExpression;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyStringLiteralExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdIndex;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.python.model.OdooRecordSetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class OdooRefCallTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getCallType(@NotNull PyFunction function,
                                   @NotNull PyCallSiteExpression callSite,
                                   @NotNull TypeEvalContext context) {
        Project project = function.getProject();
        if (OdooNames.ENV_REF_FUNC_QNAME.equals(function.getQualifiedName()) && callSite instanceof PyCallExpression) {
            PyStringLiteralExpression idExpression = ((PyCallExpression) callSite).getArgument(0, PyStringLiteralExpression.class);
            if (idExpression != null) {
                String id = idExpression.getStringValue();
                Collection<OdooRecord> records = OdooExternalIdIndex.findRecordsById(id, callSite);
                if (!records.isEmpty()) {
                    String model = records.iterator().next().getModel();
                    if (StringUtil.isNotEmpty(model)) {
                        return new Ref<>(new OdooModelClassType(model, OdooRecordSetType.ONE, project));
                    }
                }
            }
        }
        return null;
    }
}
