package dev.ngocta.pycharm.odoo.python.psi;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiPolyVariantReference;
import com.jetbrains.python.psi.impl.PyReferenceExpressionImpl;
import com.jetbrains.python.psi.impl.references.PyQualifiedReference;
import com.jetbrains.python.psi.impl.references.PyReferenceImpl;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import org.jetbrains.annotations.NotNull;

public class OdooPyReferenceExpression extends PyReferenceExpressionImpl {
    public OdooPyReferenceExpression(@NotNull ASTNode astNode) {
        super(astNode);
    }

    @NotNull
    @Override
    public PsiPolyVariantReference getReference(@NotNull PyResolveContext context) {
        PsiPolyVariantReference reference = super.getReference(context);
        if (reference instanceof PyQualifiedReference) {
            reference = new OdooPyQualifiedReference(this, context);
        } else if (reference instanceof PyReferenceImpl) {
            reference = new OdooPyReference(this, context);
        }
        return reference;
    }
}
