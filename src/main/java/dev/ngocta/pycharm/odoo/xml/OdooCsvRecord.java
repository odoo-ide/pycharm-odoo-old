package dev.ngocta.pycharm.odoo.xml;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;

public class OdooCsvRecord extends FakePsiElement implements NavigationItem {
    private final VirtualFile myFile;
    private final String myId;

    public OdooCsvRecord(@NotNull VirtualFile file, @NotNull String id) {
        myFile = file;
        myId = id;
    }

    @Override
    public PsiElement getParent() {
        return null;
    }

    @Override
    public boolean isPhysical() {
        return true;
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @Override
    public void navigate(boolean requestFocus) {

    }
}
