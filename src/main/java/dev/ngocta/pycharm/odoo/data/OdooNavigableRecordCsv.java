package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;

public class OdooNavigableRecordCsv extends FakePsiElement implements OdooNavigableRecord {
    private final VirtualFile myFile;
    private final String myId;
    private final Project myProject;

    public OdooNavigableRecordCsv(@NotNull String id, @NotNull VirtualFile file, @NotNull Project project) {
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
        return new OdooNavigableRecordPresentation(this);
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
        OdooDataUtils.processCsvRecord(myFile, (id, lineNumber) -> {
            if (id.equals(myId)) {
                (new OpenFileDescriptor(myProject, myFile, lineNumber - 1, 0)).navigate(requestFocus);
                return false;
            }
            return true;
        });
    }
}
