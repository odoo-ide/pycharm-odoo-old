package dev.ngocta.pycharm.odoo.xml;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooRecordXmlDefinition implements OdooRecordDefinition {
    private final OdooDomRecord myRecord;
    private final XmlTag myTag;

    public OdooRecordXmlDefinition(OdooDomRecord record) {
        myRecord = record;
        myTag = record.getXmlTag();
    }

    @NotNull
    @Override
    public String getId() {
        return myRecord.getQualifiedId();
    }

    @NotNull
    @Override
    public String getModel() {
        String model = myRecord.getModel().getValue();
        if (model != null) {
            return model;
        }
        return "";
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
