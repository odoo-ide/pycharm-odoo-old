package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.icons.AllIcons;
import com.intellij.psi.DelegatePsiTarget;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PomTargetPsiElementImpl;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class OdooJSFieldWidget extends PomTargetPsiElementImpl implements NavigatablePsiElement {
    private final String myName;
    private final String myOriginName;
    private final String myViewType;

    public OdooJSFieldWidget(@NotNull String name,
                             @NotNull PsiElement element) {
        super(new DelegatePsiTarget(element));
        myOriginName = name;
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
    @Nullable
    public Icon getIcon() {
        return AllIcons.Nodes.MultipleTypeDefinitions;
    }

    @Override
    @Nullable
    public String getLocationString() {
        PsiFile file = getNavigationElement().getContainingFile();
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
                getTarget().equals(that.getTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(myOriginName, getTarget());
    }

    @Override
    public String toString() {
        return "OdooJSFieldWidget{" +
                "myName='" + myOriginName + '\'' +
                ", myElement=" + getNavigationElement() +
                '}';
    }
}
