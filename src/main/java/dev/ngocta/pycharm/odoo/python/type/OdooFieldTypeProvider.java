package dev.ngocta.pycharm.odoo.python.type;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.impl.PyBuiltinCache;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.PyUnionType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.OdooNames;
import dev.ngocta.pycharm.odoo.python.OdooUtils;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClassType;
import dev.ngocta.pycharm.odoo.python.model.OdooRecordSetType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooFieldTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression, @NotNull TypeEvalContext context) {
        PsiPolyVariantReference variantReference = referenceExpression.getReference();
        PsiElement psiElement = variantReference.resolve();
        if (psiElement instanceof PyTargetExpression) {
            return CachedValuesManager.getCachedValue(psiElement, () -> {
                PyType fieldType = getFieldType((PyTargetExpression) psiElement, context);
                return CachedValueProvider.Result.createSingleDependency(fieldType, psiElement);
            });
        }

        return null;
    }

    @Nullable
    private PyType getFieldType(PyTargetExpression field, TypeEvalContext context) {
        Project project = field.getProject();
        PyExpression pyExpression = field.findAssignedValue();
        if (pyExpression instanceof PyCallExpression) {
            PyCallExpression callExpression = (PyCallExpression) pyExpression;
            PyExpression callee = callExpression.getCallee();
            if (callee != null) {
                String calleeName = callee.getName();
                PyBuiltinCache builtinCache = PyBuiltinCache.getInstance(field);
                if (calleeName != null) {
                    switch (calleeName) {
                        case OdooNames.MANY2ONE:
                        case OdooNames.ONE2MANY:
                        case OdooNames.MANY2MANY:
                            PyStringLiteralExpression comodelExpression = callExpression.getArgument(
                                    0, OdooNames.COMODEL_NAME, PyStringLiteralExpression.class);
                            if (comodelExpression != null) {
                                String comodel = comodelExpression.getStringValue();
                                OdooRecordSetType recordSetType = calleeName.equals(OdooNames.MANY2ONE) ? OdooRecordSetType.ONE : OdooRecordSetType.MULTI;
                                return OdooModelClassType.create(comodel, recordSetType, project);
                            }
                            break;
                        case OdooNames.BOOLEAN:
                            return builtinCache.getBoolType();
                        case OdooNames.INTEGER:
                            return builtinCache.getIntType();
                        case OdooNames.FLOAT:
                        case OdooNames.MONETARY:
                            return builtinCache.getFloatType();
                        case OdooNames.CHAR:
                        case OdooNames.TEXT:
                        case OdooNames.SELECTION:
                            return PyUnionType.union(builtinCache.getStrType(), null);
                        case OdooNames.DATE:
                            PyClass dateClass = OdooUtils.createClassByQName("datetime.date", field);
                            if (dateClass != null) {
                                return PyUnionType.union(context.getType(dateClass), null);
                            }
                            break;
                        case OdooNames.DATETIME:
                            PyClass datetimeClass = OdooUtils.createClassByQName("datetime.datetime", field);
                            if (datetimeClass != null) {
                                return PyUnionType.union(context.getType(datetimeClass), null);
                            }
                            break;
                    }
                }
            }
        }
        return null;
    }
}
