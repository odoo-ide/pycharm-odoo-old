package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.types.TypeEvalContext;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class OdooModelClassSuperType extends OdooModelClassType {
    private final OdooModelClassType myOrigin;
    private final PyClass myAnchor;

    public OdooModelClassSuperType(@NotNull OdooModelClassType modelClassType,
                                   @NotNull PyClass anchor) {
        super(modelClassType.getPyClass(), modelClassType.getRecordSetType());
        myOrigin = modelClassType;
        myAnchor = anchor;
    }

    @NotNull
    @Override
    public String getName() {
        return "super(" + super.getName() + ")";
    }

    @Override
    public void visitMembers(@NotNull Processor<PsiElement> processor,
                             boolean inherited,
                             @NotNull TypeEvalContext context) {
        if (inherited) {
            List<PyClass> ancestors = getPyClass().getAncestorClasses(context);
            int anchorIndex = ancestors.indexOf(myAnchor);
            if (anchorIndex >= 0) {
                ancestors = ancestors.subList(0, anchorIndex + 1);
            }
            Set<PsiElement> ignoredMembers = new THashSet<>();
            for (PyClass ancestor : ancestors) {
                ancestor.processClassLevelDeclarations((element, state) -> {
                    ignoredMembers.add(element);
                    return true;
                });
            }
            super.visitMembers(element -> {
                if (!ignoredMembers.contains(element)) {
                    return processor.process(element);
                }
                return true;
            }, true, context);
        }
    }

    public OdooModelClassType getOrigin() {
        return myOrigin;
    }
}
