package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
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
import dev.ngocta.pycharm.odoo.module.OdooModuleIndex;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class OdooExternalIdIndex extends FileBasedIndexExtension<String, OdooRecordBase> {
    private static final ID<String, OdooRecordBase> NAME = ID.create("odoo.external.id");
    private static final String EXT_CSV = "csv";
    private static final String EXT_XML = "xml";

    @NotNull
    @Override
    public ID<String, OdooRecordBase> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, OdooRecordBase, FileContent> getIndexer() {
        return inputData -> {
            Map<String, OdooRecordBase> result = new HashMap<>();
            VirtualFile file = inputData.getFile();
            PsiFile psiFile = inputData.getPsiFile();
            if (psiFile instanceof XmlFile) {
                OdooDomRoot root = OdooDataUtils.getDomRoot((XmlFile) psiFile);
                if (root != null) {
                    List<OdooDomRecord> records = root.getAllRecordVariants();
                    records.forEach(record -> {
                        String id = record.getQualifiedId(file);
                        if (id != null) {
                            result.put(id, new OdooRecordBase(id, StringUtil.notNullize(record.getModel()), record.getSubType()));
                        }
                    });
                }
            } else if (EXT_CSV.equals(file.getExtension())) {
                OdooDataUtils.processCsvRecord(file, (id, lineNumber) -> {
                    result.put(id, new OdooRecordBase(id, file.getNameWithoutExtension(), null));
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

    @NotNull
    @Override
    public DataExternalizer<OdooRecordBase> getValueExternalizer() {
        return new DataExternalizer<OdooRecordBase>() {
            @Override
            public void save(@NotNull DataOutput out, OdooRecordBase value) throws IOException {
                out.writeUTF(value.getId());
                out.writeUTF(StringUtil.notNullize(value.getModel()));
                out.writeUTF(Optional.ofNullable(value.getSubType()).map(Enum::name).orElse(""));
            }

            @Override
            public OdooRecordBase read(@NotNull DataInput in) throws IOException {
                String id = in.readUTF();
                String model = in.readUTF();
                OdooRecordSubType type = null;
                try {
                    type = OdooRecordSubType.valueOf(in.readUTF());
                } catch (IllegalArgumentException ignored) {
                }
                return new OdooRecordBase(id, model, type);
            }
        };
    }

    @Override
    public int getVersion() {
        return 5;
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

    @NotNull
    public static Collection<String> getAllIds(@NotNull Project project) {
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
                records.add(value);
                return false;
            }, GlobalSearchScope.allScope(anchor.getProject()));
        });
        return records;
    }

    @NotNull
    public static Collection<OdooNavigableRecord> findNavigableRecordById(@NotNull String id, @NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<VirtualFile> files = index.getContainingFiles(NAME, id, GlobalSearchScope.allScope(project));
        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<OdooNavigableRecord> result = new LinkedList<>();
        files.forEach(file -> {
            String extension = file.getExtension();
            if (EXT_XML.equals(extension)) {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile instanceof XmlFile) {
                    OdooDomRoot root = OdooDataUtils.getDomRoot((XmlFile) psiFile);
                    if (root != null) {
                        List<OdooDomRecord> records = root.getAllRecordVariants();
                        for (OdooDomRecord record : records) {
                            if (id.equals(record.getQualifiedId(file))) {
                                result.add(new OdooNavigableRecordXml(record));
                            }
                        }
                    }
                }
            } else if (EXT_CSV.equals(extension)) {
                result.add(new OdooNavigableRecordCsv(id, file, project));
            }
        });
        return result;
    }
}
