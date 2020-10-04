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
    private final String myNameWithViewPrefix;
    private final PsiElement myElement;
    private final String myView;

    public OdooJSFieldWidget(@NotNull String name,
                             @NotNull PsiElement element) {
        myNameWithViewPrefix = name;
        myElement = element;
        if (name.contains(".")) {
            String[] splits = name.split("\\.");
            myName = splits[1];
            myView = splits[0];
        } else {
            myName = name;
            myView = null;
        }
    }

    @Override
    @NotNull
    public String getName() {
        return myName;
    }

    @NotNull
    public String getNameWithViewPrefix() {
        return myNameWithViewPrefix;
    }

    @Override
    public String getPresentableText() {
        return getNameWithViewPrefix();
    }

    @Nullable
    public String getView() {
        return myView;
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
        return myNameWithViewPrefix.equals(that.myNameWithViewPrefix) &&
                myElement.equals(that.myElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myNameWithViewPrefix, myElement);
    }

    @Override
    public String toString() {
        return "OdooJSFieldWidget{" +
                "myName='" + myNameWithViewPrefix + '\'' +
                ", myElement=" + myElement +
                '}';
    }
}
