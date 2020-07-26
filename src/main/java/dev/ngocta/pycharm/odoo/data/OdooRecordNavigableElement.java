package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.pom.Navigatable;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooRecordNavigableElement extends FakePsiElement implements NavigatablePsiElement {
    private final OdooRecord myRecord;
    private final PsiElement myElement;

    public OdooRecordNavigableElement(@NotNull OdooRecord record,
                                      @NotNull PsiElement element) {
        myRecord = record;
        myElement = element;
    }

    @Override
    public String getName() {
        return myRecord.getQualifiedId();
    }

    @Override
    public ItemPresentation getPresentation() {
        return new OdooRecordPresentation(myRecord, myElement.getProject());
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (myElement instanceof Navigatable) {
            ((Navigatable) myElement).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public PsiElement getParent() {
        return myElement.getParent();
    }

    @Override
    @Nullable
    public TextRange getTextRange() {
        return myElement.getTextRange();
    }
}
