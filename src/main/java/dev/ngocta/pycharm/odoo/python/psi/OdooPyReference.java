package dev.ngocta.pycharm.odoo.python.psi;

import com.intellij.psi.xml.XmlFile;
import com.jetbrains.python.psi.PyQualifiedExpression;
import com.jetbrains.python.psi.impl.references.PyReferenceImpl;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class OdooPyReference extends PyReferenceImpl {
    public OdooPyReference(PyQualifiedExpression element,
                           @NotNull PyResolveContext context) {
        super(element, context);
    }

    @Override
    @NotNull
    protected List<RatedResolveResult> resolveInner() {
        if (myContext.getTypeEvalContext().getOrigin() instanceof XmlFile) {
            return Collections.emptyList();
        }
        return super.resolveInner();
    }
}
