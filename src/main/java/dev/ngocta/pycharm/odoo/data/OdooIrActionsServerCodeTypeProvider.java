package dev.ngocta.pycharm.odoo.data;

import com.google.common.collect.ImmutableSet;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ArrayUtil;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooPyUtils;
import dev.ngocta.pycharm.odoo.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.model.OdooRecordSetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class OdooIrActionsServerCodeTypeProvider extends PyTypeProviderBase {
    private final Set<String> AVAILABLE_VARIABLES = ImmutableSet.of("env", "model", "record", "records");

    @Nullable
    @Override
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression,
                                             @NotNull TypeEvalContext context) {
        String name = referenceExpression.getName();
        if (name == null || !AVAILABLE_VARIABLES.contains(name)) {
            return null;
        }
        if (referenceExpression.isQualified()) {
            return null;
        }
        String model = getBindingModel(referenceExpression);
        if (model == null) {
            return null;
        }
        Project project = referenceExpression.getProject();
        if (ArrayUtil.contains(name, "model", "record", "records")) {
            return new OdooModelClassType(model, OdooRecordSetType.MULTI, project);
        } else if ("env".equals(name)) {
            return OdooPyUtils.getEnvironmentType(referenceExpression);
        }
        return null;
    }

    private String getBindingModel(PyReferenceExpression referenceExpression) {
        PsiFile file = referenceExpression.getContainingFile();
        if (file == null) {
            return null;
        }
        PsiElement context = file.getContext();
        if (context instanceof XmlText) {
            return getBindingModelInXmlText((XmlText) context);
        }
        return null;
    }

    private String getBindingModelInXmlText(XmlText context) {
        XmlTag tag = PsiTreeUtil.getParentOfType(context, XmlTag.class);
        if (tag == null) {
            return null;
        }
        Project project = context.getProject();
        DomManager domManager = DomManager.getDomManager(project);
        DomElement domElement = domManager.getDomElement(tag);
        if (!(domElement instanceof OdooDomFieldAssignment)) {
            return null;
        }
        OdooDomFieldAssignment fieldAssignment = (OdooDomFieldAssignment) domElement;
        if (!"code".equals(fieldAssignment.getName().getStringValue())) {
            return null;
        }
        if (!ArrayUtil.contains(fieldAssignment.getModel(), OdooNames.IR_ACTIONS_SERVER, OdooNames.IR_CRON)) {
            return null;
        }
        OdooDomRecord record = fieldAssignment.getRecord();
        if (record == null) {
            return null;
        }
        for (OdooDomFieldAssignment field : record.getFields()) {
            if ("model_id".equals(field.getName().getStringValue())) {
                return field.getRefModel();
            }
        }
        return null;
    }
}
