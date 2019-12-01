package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProfiler;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.PyUnionType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooWebRequestTypeProvider extends PyTypeProviderBase {
    @Override
    public Ref<PyType> getReferenceType(@NotNull PsiElement referenceTarget, @NotNull TypeEvalContext context, @Nullable PsiElement anchor) {
        if (referenceTarget instanceof PyTargetExpression && OdooNames.REQUEST_QNAME.equals(((PyTargetExpression) referenceTarget).getQualifiedName())) {
            return CachedValuesManager.getCachedValue(referenceTarget, () -> {
                Ref<PyType> result = null;
                PyType httpRequestType = OdooUtils.getClassTypeByQName(OdooNames.HTTP_REQUEST_CLASS_NAME, referenceTarget, false);
                PyType jsonRequestType = OdooUtils.getClassTypeByQName(OdooNames.JSON_REQUEST_CLASS_NAME, referenceTarget, false);
                if (httpRequestType != null && jsonRequestType != null) {
                    result = Ref.create(PyUnionType.union(httpRequestType, jsonRequestType));
                }
                return CachedValueProvider.Result.create(result, referenceTarget);
            });
        }
        return null;
    }
}
