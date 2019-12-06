package dev.ngocta.pycharm.odoo.xml;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;

public class OdooRecordCsvDefinition extends FakePsiElement implements OdooRecordDefinition {
    private final VirtualFile myFile;
    private final String myId;
    private final Project myProject;

    public OdooRecordCsvDefinition(@NotNull String id, @NotNull VirtualFile file, @NotNull Project project) {
        myId = id;
        myFile = file;
        myProject = project;
    }

    @Override
    public String getName() {
        return getId();
    }

    @Override
    public PsiElement getParent() {
        return null;
    }

    @Override
    public PsiFile getContainingFile() {
        return null;
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @NotNull
    @Override
    public Project getProject() {
        return myProject;
    }

    @Override
    public ItemPresentation getPresentation() {
        return new OdooRecordPresentation(this);
    }

    @NotNull
    @Override
    public String getId() {
        return myId;
    }

    @NotNull
    @Override
    public String getModel() {
        return getFile().getNameWithoutExtension();
    }

    @NotNull
    @Override
    public VirtualFile getFile() {
        return myFile;
    }

    @NotNull
    @Override
    public PsiElement getNavigationElement() {
        return this;
    }

    @Override
    public boolean canNavigate() {
        return myFile.isValid();
    }

    @Override
    public boolean canNavigateToSource() {
        return canNavigate();
    }

    @Override
    public void navigate(boolean requestFocus) {
        (new OpenFileDescriptor(myProject, myFile)).navigate(requestFocus);
    }
}
