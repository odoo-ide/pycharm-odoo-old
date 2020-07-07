package dev.ngocta.pycharm.odoo.csv;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooCsvRecord extends FakePsiElement implements Navigatable {
    private final VirtualFile myFile;
    private final Project myProject;
    private final String myRecordId;

    public OdooCsvRecord(@NotNull VirtualFile file,
                         @NotNull Project project,
                         @NotNull String recordId) {
        myFile = file;
        myProject = project;
        myRecordId = recordId;
    }

    @Override
    public PsiElement getParent() {
        return null;
    }

    @NotNull
    public VirtualFile getContainingVirtualFile() {
        return myFile;
    }

    @Override
    public PsiFile getContainingFile() {
        return null;
    }

    @Nullable
    public String getModel() {
        return myFile.getNameWithoutExtension();
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
        OdooCsvUtils.processRecordInCsvFile(myFile, myProject, (record, lineNumber) -> {
            if (myRecordId.equals(record.getId())) {
                Navigatable navigatable = (new OpenFileDescriptor(myProject, myFile, lineNumber - 1, 0));
                navigatable.navigate(requestFocus);
                return false;
            }
            return true;
        });
    }
}
