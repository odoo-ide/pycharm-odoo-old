package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import dev.ngocta.pycharm.odoo.module.OdooModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class OdooExternalIdIndex extends FileBasedIndexExtension<String, String> {
    private static final ID<String, String> NAME = ID.create("odoo.external.id");
    private static final String EXT_CSV = "csv";
    private static final String EXT_XML = "xml";

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
            VirtualFile file = inputData.getFile();
            PsiFile psiFile = inputData.getPsiFile();
            if (psiFile instanceof XmlFile) {
                OdooDomRoot root = getDomRoot((XmlFile) psiFile);
                if (root != null) {
                    List<OdooDomRecord> tags = root.getAllRecordVariants();
                    tags.forEach(tag -> {
                        String id = tag.getQualifiedId(file);
                        if (id != null) {
                            result.put(id, tag.getModel());
                        }
                    });
                }
            } else if (EXT_CSV.equals(file.getExtension())) {
                OdooDataUtils.processCsvRecord(file, (id, lineNumber) -> {
                    result.put(id, file.getNameWithoutExtension());
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
    public @NotNull DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 4;
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
    public static Collection<String> getAllExternalIds(@NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        return index.getAllKeys(NAME, project);
    }

    public static List<String> getAvailableIds(@NotNull PsiElement anchor) {
        List<PsiDirectory> modules = OdooModuleIndex.getFlattenedDependsGraph(anchor);
        if (modules.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> moduleNames = modules.stream().map(PsiDirectory::getName).collect(Collectors.toList());
        List<String> ids = new LinkedList<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        Project project = anchor.getProject();
        index.processAllKeys(NAME, id -> {
            String[] splits = id.split("\\.");
            if (splits.length > 1 && moduleNames.contains(splits[0])) {
                ids.add(id);
            }
            return true;
        }, project);
        return ids;
    }

    @NotNull
    public static List<OdooRecord> getAvailableRecords(@NotNull PsiElement anchor) {
        List<String> ids = getAvailableIds(anchor);
        List<OdooRecord> records = new LinkedList<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        ids.forEach(id -> {
            index.processValues(NAME, id, null, (file, value) -> {
                records.add(new OdooRecordBase(id, value));
                return false;
            }, GlobalSearchScope.allScope(anchor.getProject()));
        });
        return records;
    }

    @NotNull
    public static Collection<OdooRecordItem> findRecordItemByExternalId(@NotNull String id, @NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<VirtualFile> files = index.getContainingFiles(NAME, id, GlobalSearchScope.allScope(project));
        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<OdooRecordItem> result = new LinkedList<>();
        files.forEach(file -> {
            String extension = file.getExtension();
            if (EXT_XML.equals(extension)) {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile instanceof XmlFile) {
                    OdooDomRoot root = getDomRoot((XmlFile) psiFile);
                    if (root != null) {
                        List<OdooDomRecord> records = root.getAllRecordVariants();
                        for (OdooDomRecord record : records) {
                            if (id.equals(record.getQualifiedId(file))) {
                                result.add(new OdooRecordItemXml(record));
                            }
                        }
                    }
                }
            } else if (EXT_CSV.equals(extension)) {
                result.add(new OdooRecordItemCsv(id, file, project));
            }
        });
        return result;
    }
}
