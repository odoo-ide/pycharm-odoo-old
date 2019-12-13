package dev.ngocta.pycharm.odoo.data;

import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyFunctionType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooExternalIdReferenceContributor extends PsiReferenceContributor {
    public static final PsiElementPattern.Capture<PyStringLiteralExpression> REF_PATTERN =
            psiElement(PyStringLiteralExpression.class).withParent(
                    psiElement(PyArgumentList.class).withParent(
                            psiElement(PyCallExpression.class).with(new PatternCondition<PyCallExpression>("ref") {
                                @Override
                                public boolean accepts(@NotNull PyCallExpression call, ProcessingContext context) {
                                    PyExpression callee = call.getCallee();
                                    if (callee != null) {
                                        TypeEvalContext typeEvalContext = TypeEvalContext.userInitiated(call.getProject(), call.getContainingFile());
                                        PyType calleeType = typeEvalContext.getType(callee);
                                        if (calleeType instanceof PyFunctionType) {
                                            PyCallable callable = ((PyFunctionType) calleeType).getCallable();
                                            return callable instanceof PyFunction && OdooNames.REF_QNAME.equals(callable.getQualifiedName());
                                        }
                                    }
                                    return false;
                                }
                            })));

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        registrar.registerReferenceProvider(REF_PATTERN, new OdooExternalIdReferenceProvider());
    }
}
