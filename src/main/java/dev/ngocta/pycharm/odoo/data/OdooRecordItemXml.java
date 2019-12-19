package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooRecordItemXml implements OdooRecordItem {
    private final OdooDomRecord myRecord;
    private final XmlTag myTag;

    public OdooRecordItemXml(OdooDomRecord record) {
        myRecord = record;
        myTag = record.getXmlTag();
    }

    @NotNull
    @Override
    public String getId() {
        return myRecord.getQualifiedId();
    }

    @Nullable
    @Override
    public String getModel() {
        return myRecord.getModel();
    }

    @Nullable
    @Override
    public VirtualFile getFile() {
        PsiFile file = myTag.getContainingFile();
        if (file != null) {
            return file.getVirtualFile();
        }
        return null;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        return myTag;
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (myTag instanceof Navigatable) {
            ((Navigatable) myTag).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        if (myTag instanceof Navigatable) {
            return ((Navigatable) myTag).canNavigate();
        }
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        if (myTag instanceof Navigatable) {
            return ((Navigatable) myTag).canNavigateToSource();
        }
        return false;
    }
}
