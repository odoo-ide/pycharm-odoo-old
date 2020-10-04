package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.icons.AllIcons;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.util.TextRange;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.FakePsiElement;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class OdooJSFieldWidget extends FakePsiElement implements NavigationItem {
    private final String myName;
    private final String myOriginName;
    private final PsiElement myElement;
    private final String myViewType;

    public OdooJSFieldWidget(@NotNull String name,
                             @NotNull PsiElement element) {
        myOriginName = name;
        myElement = element;
        if (name.contains(".")) {
            String[] splits = name.split("\\.");
            myName = splits[1];
            myViewType = splits[0];
        } else {
            myName = name;
            myViewType = null;
        }
    }

    @Override
    @NotNull
    public String getName() {
        return myName;
    }

    @NotNull
    public String getOriginName() {
        return myOriginName;
    }

    @Override
    public String getPresentableText() {
        return getOriginName();
    }

    @Nullable
    public String getViewType() {
        return myViewType;
    }

    @Override
    public PsiElement getParent() {
        return myElement.getParent();
    }

    @Override
    public PsiFile getContainingFile() {
        return myElement.getContainingFile();
    }

    @Override
    public PsiElement getOriginalElement() {
        return myElement;
    }

    @Override
    @Nullable
    public TextRange getTextRange() {
        return myElement.getTextRange();
    }

    @Override
    public boolean canNavigate() {
        return myElement instanceof Navigatable;
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (myElement instanceof Navigatable) {
            ((Navigatable) myElement).navigate(requestFocus);
        }
    }

    @Override
    @Nullable
    public Icon getIcon(boolean open) {
        return AllIcons.Nodes.MultipleTypeDefinitions;
    }

    @Override
    @Nullable
    public String getLocationString() {
        PsiFile file = myElement.getContainingFile();
        if (file != null) {
            return OdooModuleUtils.getLocationStringForFile(file.getVirtualFile());
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooJSFieldWidget that = (OdooJSFieldWidget) o;
        return myOriginName.equals(that.myOriginName) &&
                myElement.equals(that.myElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myOriginName, myElement);
    }

    @Override
    public String toString() {
        return "OdooJSFieldWidget{" +
                "myName='" + myOriginName + '\'' +
                ", myElement=" + myElement +
                '}';
    }
}
