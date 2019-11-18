package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.python.codeInsight.PyCustomMember;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.types.PyModuleMembersProvider;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class OdooModuleMembersProvider extends PyModuleMembersProvider {
    @NotNull
    @Override
    protected Collection<PyCustomMember> getMembersByQName(@NotNull PyFile pyFile, @NotNull String s, @NotNull TypeEvalContext typeEvalContext) {
        Project project = pyFile.getProject();
        if (s.equals("odoo.addons")) {
            Collection<PsiDirectory> modules = OdooModuleIndex.getAllModules(project);
            Collection<PyCustomMember> members = new ArrayList<>();
            for (PsiDirectory module : modules) {
                PyCustomMember member = new PyCustomMember(module.getName(), module);
                members.add(member);
            }
            return members;
        }
        return Collections.emptyList();
    }
}
