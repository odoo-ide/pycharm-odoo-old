package dev.ngocta.pycharm.odoo.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class OdooXmlIdIndex extends FileBasedIndexExtension<String, String> {
    private static final ID<String, String> NAME = ID.create("odoo.xml.id");

    @NotNull
    @Override
    public ID<String, String> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            Map<String, String> result = new HashMap<>();
            PsiFile file = inputData.getPsiFile();
            if (file instanceof XmlFile) {
                PsiDirectory module = OdooUtils.getOdooModuleDir(file);
                if (module != null) {
                    DomManager manager = DomManager.getDomManager(file.getProject());
                    DomFileElement<OdooRootTag> fileElement = manager.getFileElement((XmlFile) file, OdooRootTag.class);
                    if (fileElement != null) {
                        OdooRootTag root = fileElement.getRootElement();
                        List<OdooRecordTag> tags = root.getRecords();
                        root.getData().forEach(data -> {
                            tags.addAll(data.getRecords());
                        });
                        tags.forEach(tag -> {
                            String id = tag.getId().getValue();
                            String model = tag.getModel().getValue();
                            if (id != null && model != null) {
                                result.put(id, model);
                            }
                        });
                    }
                }
            }
            return result;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(XmlFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }
}
