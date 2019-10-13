import com.google.gson.Gson;
import com.intellij.psi.PsiFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class OdooModelIndex extends FileBasedIndexExtension<String, String> {
    public static final @NotNull ID<String, String> NAME = ID.create("odoo.model");

    private DataIndexer<String, String, FileContent> myDataIndexer = inputData -> {
        PsiFile psiFile = inputData.getPsiFile();
        HashMap<String, String> result = new HashMap<>();
        if (psiFile instanceof PyFile) {
            PyFile pyFile = (PyFile) psiFile;
            pyFile.getTopLevelClasses().forEach(pyClass -> {
                String model = null;
                Boolean isPrimary = false;
                List<String> inherits = null;
                PyTargetExpression nameEx = pyClass.findClassAttribute("_name", false, null);
                if (nameEx != null) {
                    PyExpression valueEx = nameEx.findAssignedValue();
                    if (valueEx instanceof PyStringLiteralExpression) {
                        model = ((PyStringLiteralExpression) valueEx).getStringValue();
                        isPrimary = true;
                    }
                }
                PyTargetExpression inheritEx = pyClass.findClassAttribute("_inherit", false, null);
                if (inheritEx != null) {
                    PyExpression valueEx = inheritEx.findAssignedValue();
                    if (valueEx instanceof PyStringLiteralExpression) {
                        String inheritModel = ((PyStringLiteralExpression) valueEx).getStringValue();
                        if (model == null) {
                            model = inheritModel;
                        } else {
                            inherits = Collections.singletonList(inheritModel);
                        }
                    } else {
                        inherits = PyUtil.strListValue(valueEx);
                    }
                }
                if (model != null) {
                    HashMap<String, Object> info = new HashMap<>();
                    info.put("primary", isPrimary);
                    info.put("class", pyClass.getName());
                    info.put("inherits", inherits);
                    String infoJson = new Gson().toJson(info);
                    result.put(model, infoJson);
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
    public FileBasedIndex.@NotNull InputFilter getInputFilter() {
        return OdooModelInputFilter.INSTANCE;
    }

    @Override
    public boolean dependsOnFileContent() {
        return false;
    }
}
