package dev.ngocta.pycharm.odoo.model;

import com.jetbrains.python.psi.PyListLiteralExpression;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.resolve.PyReferenceResolveProvider;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class OdooSearchDomainFieldReferenceResolveProvider implements PyReferenceResolveProvider {
    @Override
    @NotNull
    public List<RatedResolveResult> resolveName(@NotNull PyQualifiedExpression pyQualifiedExpression,
                                                @NotNull TypeEvalContext typeEvalContext) {
        String name = pyQualifiedExpression.getReferencedName();
        if (name == null || pyQualifiedExpression.getQualifier() != null) {
            return Collections.emptyList();
        }
        PyListLiteralExpression domainExpression = OdooModelUtils.getSearchDomainExpression(pyQualifiedExpression);
        if (domainExpression != null) {
            OdooModelClass modelClass = OdooModelUtils.resolveSearchDomainContext(domainExpression, false);
            if (modelClass != null) {
                PyTargetExpression field = modelClass.findField(name, typeEvalContext);
                if (field != null) {
                    return Collections.singletonList(new RatedResolveResult(RatedResolveResult.RATE_NORMAL, field));
                }
            }
        }
        return Collections.emptyList();
    }
}
