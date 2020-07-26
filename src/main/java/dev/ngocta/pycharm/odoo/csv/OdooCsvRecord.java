package dev.ngocta.pycharm.odoo.csv;

import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.FakePsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooCsvRecord extends FakePsiElement implements Navigatable {
    private final VirtualFile myFile;
    private final Project myProject;
    private final String myId;

    public OdooCsvRecord(@NotNull VirtualFile file,
                         @NotNull Project project,
                         @NotNull String id) {
        myFile = file;
        myProject = project;
        myId = id;
    }

    @Override
    public PsiElement getParent() {
        return getContainingFile();
    }

    @NotNull
    public VirtualFile getContainingVirtualFile() {
        return myFile;
    }

    @Override
    public PsiFile getContainingFile() {
        return PsiManager.getInstance(myProject).findFile(myFile);
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
        OdooCsvUtils.processRecordInCsvFile(myFile, myProject, (record, csvRecord) -> {
            if (myId.equals(record.getId())) {
                Navigatable navigatable = (new OpenFileDescriptor(myProject, myFile, (int) csvRecord.getParser().getCurrentLineNumber() - 1, 0));
                navigatable.navigate(requestFocus);
                return false;
            }
            return true;
        });
    }

    @Override
    @Nullable
    public TextRange getTextRange() {
        Ref<TextRange> rangeRef = Ref.create();
        OdooCsvUtils.processRecordInCsvFile(myFile, myProject, (record, csvRecord) -> {
            if (myId.equals(record.getId())) {
                int start = (int) csvRecord.getCharacterPosition();
                rangeRef.set(new TextRange(start, start));
                return false;
            }
            return true;
        });
        return rangeRef.get();
    }
}
