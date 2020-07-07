package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.codeInsight.PyCustomMember;
import com.jetbrains.python.psi.PyCallExpression;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpression;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyOverridingClassMembersProvider;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OdooSuperMembersProvider implements PyOverridingClassMembersProvider {
    @NotNull
    @Override
    public Collection<PyCustomMember> getMembers(PyClassType type,
                                                 PsiElement location,
                                                 @NotNull TypeEvalContext context) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public PsiElement resolveMember(@NotNull PyClassType type,
                                    @NotNull String name,
                                    @Nullable PsiElement location,
                                    @NotNull PyResolveContext resolveContext) {
        Ref<PsiElement> ref = new Ref<>();
        TypeEvalContext context = resolveContext.getTypeEvalContext();
        if (location instanceof PyCallExpression) {
            PyExpression callee = ((PyCallExpression) location).getCallee();
            if (callee != null && PyNames.SUPER.equals(callee.getName())) {
                PyClass instanceClass = null;
                PyClass superClass = null;
                PyExpression[] args = ((PyCallExpression) location).getArguments();
                if (args.length == 2) {
                    PyType superType = context.getType(args[0]);
                    if (superType instanceof PyClassType) {
                        superClass = ((PyClassType) superType).getPyClass();
                    }
                    PyType instanceType = context.getType(args[1]);
                    if (instanceType instanceof PyClassType) {
                        instanceClass = ((PyClassType) instanceType).getPyClass();
                    }
                } else if (args.length == 0) {
                    PyClass cls = PsiTreeUtil.getParentOfType(location, PyClass.class);
                    if (cls != null) {
                        OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(cls);
                        if (modelClass != null) {
                            instanceClass = modelClass;
                            superClass = cls;
                        }
                    }
                }
                if (instanceClass != null && superClass != null) {
                    visitSuperMembers(instanceClass, superClass, context, member -> {
                        if (name.equals(member.getName())) {
                            ref.set(member);
                            return false;
                        }
                        return true;
                    });
                }
            }
        }
        return ref.get();
    }

    private void visitSuperMembers(@NotNull PyClass instanceClass,
                                   @NotNull PyClass superClass,
                                   @NotNull TypeEvalContext context,
                                   @NotNull Processor<PsiNamedElement> processor) {
        List<PyClass> ancestors = instanceClass.getAncestorClasses(context);
        int idx = ancestors.indexOf(superClass);
        if (idx >= 0) {
            for (PyClass parent : ancestors.subList(idx + 1, ancestors.size())) {
                if (!parent.visitMethods(processor::process, false, context)) {
                    break;
                }
            }
        }
    }
}
