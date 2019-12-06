package dev.ngocta.pycharm.odoo.xml;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Processor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.*;

public class OdooXmlIdIndex extends ScalarIndexExtension<String> {
    private static final ID<String, Void> NAME = ID.create("odoo.xml.id");
    private static final String EXT_CSV = "csv";
    private static final String EXT_XML = "xml";

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> result = new HashMap<>();
            VirtualFile file = inputData.getFile();
            PsiFile psiFile = inputData.getPsiFile();
            if (psiFile instanceof XmlFile) {
                OdooDomRoot root = getDomRoot((XmlFile) psiFile);
                if (root != null) {
                    List<OdooDomRecord> tags = root.getAllRecords();
                    tags.forEach(tag -> {
                        String id = tag.getQualifiedId(file);
                        if (id != null) {
                            result.put(id, null);
                        }
                    });
                }
            } else if (EXT_CSV.equals(file.getExtension())) {
                processCsvRecord(file, id -> {
                    result.put(id, null);
                    return true;
                });
            }
            return result;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> {
            String extension = file.getExtension();
            return EXT_XML.equals(extension) || EXT_CSV.equals(extension);
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    private static void processCsvRecord(@NotNull VirtualFile file, @NotNull Processor<String> processor) {
        VirtualFile moduleDirectory = OdooUtils.getOdooModuleDirectory(file);
        if (moduleDirectory == null) {
            return;
        }
        try {
            InputStream inputStream = file.getInputStream();
            CSVParser parser = CSVParser.parse(inputStream, file.getCharset(), CSVFormat.DEFAULT.withHeader());
            if (!parser.getHeaderNames().contains("id")) {
                return;
            }
            for (CSVRecord strings : parser) {
                String id = strings.get("id");
                if (id != null) {
                    if (!id.contains(".")) {
                        id = moduleDirectory.getName() + "." + id;
                    }
                    if (!processor.process(id)) {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    private static OdooDomRoot getDomRoot(@NotNull XmlFile xmlFile) {
        DomManager domManager = DomManager.getDomManager(xmlFile.getProject());
        DomFileElement<OdooDomRoot> fileElement = domManager.getFileElement(xmlFile, OdooDomRoot.class);
        if (fileElement != null) {
            return fileElement.getRootElement();
        }
        return null;
    }

    @NotNull
    public static Collection<String> getAllXmlIds(@NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        return index.getAllKeys(NAME, project);
    }

    @NotNull
    public static Collection<OdooRecordDefinition> findRecordDefinitions(@NotNull String id, @NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<VirtualFile> files = index.getContainingFiles(NAME, id, GlobalSearchScope.allScope(project));
        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<OdooRecordDefinition> result = new LinkedList<>();
        files.forEach(file -> {
            String extension = file.getExtension();
            if (EXT_XML.equals(extension)) {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile instanceof XmlFile) {
                    OdooDomRoot root = getDomRoot((XmlFile) psiFile);
                    if (root != null) {
                        List<OdooDomRecord> records = root.getAllRecords();
                        for (OdooDomRecord record : records) {
                            if (id.equals(record.getQualifiedId(file))) {
                                result.add(new OdooRecordXmlDefinition(record));
                            }
                        }
                    }
                }
            } else if (EXT_CSV.equals(extension)) {
                processCsvRecord(file, s -> {
                    if (id.equals(s)) {
                        result.add(new OdooRecordCsvDefinition(s, file, project));
                        return false;
                    }
                    return true;
                });
            }
        });
        return result;
    }
}
