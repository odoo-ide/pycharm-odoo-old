import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.psi.PyFile;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class OdooModelNameIndex extends FileBasedIndexExtension<String, String> {
    public static final @NotNull ID<String, String> NAME = ID.create("odoo.model.name");

    private DataIndexer<String, String, FileContent> myDataIndexer = inputData -> {
        VirtualFile virtualFile = inputData.getFile();
        PsiFile psiFile = PsiManager.getInstance(inputData.getProject()).findFile(virtualFile);
        HashMap<String, String> result = new HashMap<>();
        if (psiFile instanceof PyFile) {
            PyFile pyFile = (PyFile) psiFile;
            pyFile.getTopLevelClasses().forEach(pyClass -> {
                OdooModelInfo info = OdooModelInfo.readFromClass(pyClass);
                if (info != null) {
                    result.put(info.getName(), pyClass.getQualifiedName());
                }
            });
        }
        return result;
    };

    @Override
    public @NotNull ID<String, String> getName() {
        return NAME;
    }

    @Override
    public @NotNull DataIndexer<String, String, FileContent> getIndexer() {
        return myDataIndexer;
    }

    @Override
    public @NotNull KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public @NotNull DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public @NotNull FileBasedIndex.InputFilter getInputFilter() {
        return OdooModelInputFilter.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
