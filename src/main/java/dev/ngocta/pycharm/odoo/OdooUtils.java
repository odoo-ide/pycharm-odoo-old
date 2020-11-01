package dev.ngocta.pycharm.odoo;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.resolve.FileContextUtil;
import com.intellij.psi.search.GlobalSearchScope;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
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

    public static boolean isInOdooPackage(@Nullable PsiElement element) {
        PsiFile file = getOriginalContextFile(element);
        if (file == null) {
            return false;
        }
        PsiDirectory directory = file.getParent();
        while (directory != null) {
            if (directory.findFile("odoo-bin") != null) {
                return true;
            }
            directory = directory.getParent();
        }
        return false;
    }

    public static boolean isOdooCode(@Nullable PsiElement element) {
        if (element == null) {
            return false;
        }
        if (OdooModuleUtils.isInOdooModule(element)) {
            return true;
        }
        return isInOdooPackage(element);
    }
}
