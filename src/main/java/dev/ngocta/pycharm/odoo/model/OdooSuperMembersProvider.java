package dev.ngocta.pycharm.odoo.model;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.Processor;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.codeInsight.PyCustomMember;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.PyClassMembersProvider;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class OdooSuperMembersProvider implements PyClassMembersProvider {
    @NotNull
    @Override
    public Collection<PyCustomMember> getMembers(PyClassType type, PsiElement location, @NotNull TypeEvalContext context) {
        List<PyCustomMember> result = new LinkedList<>();
        visitSuperMembers(type, context, member -> {
            if (member.getName() != null) {
                result.add(new PyCustomMember(member.getName(), member));
            }
            return true;
        });
        return result;
    }

    @Nullable
    @Override
    public PsiElement resolveMember(@NotNull PyClassType type, @NotNull String name, @Nullable PsiElement location, @NotNull PyResolveContext resolveContext) {
        Ref<PsiElement> ref = new Ref<>();
        if (location instanceof PyCallExpression) {
            PyExpression callee = ((PyCallExpression) location).getCallee();
            if (callee != null && PyNames.SUPER.equals(callee.getName())) {
                visitSuperMembers(type, resolveContext.getTypeEvalContext(), member -> {
                    if (name.equals(member.getName())) {
                        ref.set(member);
                        return false;
                    }
                    return true;
                });
            }
        }
        return ref.get();
    }

    private void visitSuperMembers(@NotNull PyClassType type, @NotNull TypeEvalContext context, @NotNull Processor<PsiNamedElement> processor) {
        PyClass cls = type.getPyClass();
        OdooModelClass modelCls = cls.getUserData(OdooModelClass.MODEL_CLASS);
        if (modelCls != null) {
            List<PyClass> ancestors = modelCls.getAncestorClasses(context);
            int idx = ancestors.indexOf(cls);
            if (idx >= 0) {
                for (PyClass parent : ancestors.subList(idx, ancestors.size())) {
                    if (!parent.visitMethods(processor::process, false, context)) {
                        break;
                    }
                }
            }
        }
    }
}
