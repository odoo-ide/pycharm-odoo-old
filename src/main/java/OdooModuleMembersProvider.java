import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.jetbrains.python.codeInsight.PyCustomMember;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.types.PyModuleMembersProvider;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class OdooModuleMembersProvider extends PyModuleMembersProvider {
    @Override
    protected @NotNull Collection<PyCustomMember> getMembersByQName(@NotNull PyFile pyFile, @NotNull String s, @NotNull TypeEvalContext typeEvalContext) {
        Project project = pyFile.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.allScope(project);
        if (s.equals("odoo.addons")) {
            Collection<PsiDirectory> addons = new ArrayList<>();
            FilenameIndex.processFilesByName("__manifest__.py", false, psiFileSystemItem -> {
                PsiFileSystemItem addon = psiFileSystemItem.getParent();
                if (addon instanceof PsiDirectory) {
                    addons.add((PsiDirectory) addon);
                }
                return true;
            }, searchScope, project, null);
            Collection<PyCustomMember> members = new ArrayList<>();
            for (PsiDirectory addon : addons) {
                PyCustomMember member = new PyCustomMember(addon.getName(), addon);
                members.add(member);
            }
            return members;
        }
        return Collections.emptyList();
    }
}
