package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class OdooUtils {
    private OdooUtils() {

    }

    @NotNull
    public static GlobalSearchScope getProjectModuleWithDependenciesScope(@NotNull PsiElement anchor) {
        Module module = ModuleUtil.findModuleForPsiElement(anchor);
        if (module != null) {
            return module.getModuleContentWithDependenciesScope().union(module.getModuleWithLibrariesScope());
        }
        return GlobalSearchScope.projectScope(anchor.getProject());
    }

    public static void writeNullableString(@Nullable String value,
                                           @NotNull DataOutput out) throws IOException {
        out.writeBoolean(value != null);
        if (value != null) {
            out.writeUTF(value);
        }
    }

    @Nullable
    public static String readNullableString(@NotNull DataInput in) throws IOException {
        return in.readBoolean() ? in.readUTF() : null;
    }

    @Nullable
    public static PsiFile getOriginalContextFile(@Nullable PsiElement element) {
        if (element == null) {
            return null;
        }
        PsiFile file = FileContextUtil.getContextFile(element);
        if (file == null) {
            return null;
        }
        return file.getOriginalFile();
    }
}
