package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.openapi.util.TextRange;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class OdooJSFieldWidget extends FakePsiElement {
    private final String myName;
    private final PsiElement myElement;

    public OdooJSFieldWidget(@NotNull String name,
                             @NotNull PsiElement element) {

        myName = name;
        myElement = element;
    }

    @Override
    public String getName() {
        return myName;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooJSFieldWidget that = (OdooJSFieldWidget) o;
        return myName.equals(that.myName) &&
                myElement.equals(that.myElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myName, myElement);
    }
}
