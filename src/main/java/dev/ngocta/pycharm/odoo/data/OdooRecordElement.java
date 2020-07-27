package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.util.PlatformIcons;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdooRecordElement extends FakePsiElement implements NavigatablePsiElement {
    private final OdooRecord myRecord;
    private final PsiElement myElement;

    public OdooRecordElement(@NotNull OdooRecord record,
                             @NotNull PsiElement element) {
        myRecord = record;
        myElement = element;
    }

    @Override
    public String getName() {
        return myRecord.getQualifiedId();
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

    public String getPresentableText() {
        String text = myRecord.getQualifiedId();
        if (StringUtil.isNotEmpty(myRecord.getModel())) {
            text += " (" + myRecord.getModel() + ")";
        }
        return text;
    }

    @Override
    public String getLocationString() {
        VirtualFile file = myRecord.getDataFile();
        if (file == null) {
            return null;
        }
        String path = file.getPath();
        OdooModule module = OdooModuleUtils.getContainingOdooModule(file, getProject());
        if (module != null) {
            path = "/" + module.getName() + path.substring(module.getDirectory().getVirtualFile().getPath().length());
        }
        return path;
    }

    @Override
    public Icon getIcon(boolean unused) {
        return PlatformIcons.XML_TAG_ICON;
    }
}
