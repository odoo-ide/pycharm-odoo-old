package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class OdooRecordNavigationItem implements NavigationItem {
    private final OdooRecord myRecord;
    private final PsiElement myElement;

    public OdooRecordNavigationItem(@NotNull OdooRecord record,
                                    @NotNull PsiElement element) {
        myRecord = record;
        myElement = element;
    }

    @Override
    public String getName() {
        return myRecord.getId();
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
    public boolean canNavigateToSource() {
        return true;
    }
}
