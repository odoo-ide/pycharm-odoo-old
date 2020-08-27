package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.PyForPart;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooRecordInSelfTypeProvider extends PyTypeProviderBase {
    @Override
    public Ref<PyType> getReferenceType(@NotNull PsiElement element,
                                        @NotNull TypeEvalContext context,
                                        @Nullable PsiElement anchor) {
        if (element instanceof PyTargetExpression && element.getParent() instanceof PyForPart) {
            PyExpression source = ((PyForPart) element.getParent()).getSource();
            if (source != null) {
                PyType type = context.getType(source);
                if (type instanceof OdooModelClassType) {
                    type = ((OdooModelClassType) type).withOneRecord();
                    return Ref.create(type);
                }
            }
        }
        return null;
    }
}
