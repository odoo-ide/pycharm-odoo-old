import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.QualifiedName;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.codeInsight.PyCustomMember;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.PyClassMembersProvider;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyOverridingClassMembersProvider;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class OdooClassMembersProvider implements PyOverridingClassMembersProvider {
    @Override
    public @NotNull Collection<PyCustomMember> getMembers(PyClassType pyClassType, PsiElement psiElement, @NotNull TypeEvalContext typeEvalContext) {
        return Collections.emptyList();
    }

    @Override
    public @Nullable PsiElement resolveMember(@NotNull PyClassType pyClassType, @NotNull String s, @Nullable PsiElement psiElement, @NotNull PyResolveContext pyResolveContext) {
        return null;
    }
}
