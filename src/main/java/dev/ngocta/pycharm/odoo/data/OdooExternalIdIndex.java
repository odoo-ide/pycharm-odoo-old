package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Processor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.model.OdooModelIndex;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OdooExternalIdIndex extends FileBasedIndexExtension<String, OdooRecord> {
    private static final ID<String, OdooRecord> NAME = ID.create("odoo.external.id");

    @NotNull
    @Override
    public ID<String, OdooRecord> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, OdooRecord, FileContent> getIndexer() {
        return inputData -> {
            Map<String, OdooRecord> result = new HashMap<>();
            VirtualFile file = inputData.getFile();
            Project project = inputData.getProject();
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (psiFile instanceof XmlFile) {
                OdooDomRoot root = OdooDataUtils.getDomRoot((XmlFile) psiFile);
                if (root != null) {
                    List<OdooDomRecordLike> items = root.getAllRecordLikeItems();
                    items.forEach(item -> {
                        OdooRecord record = item.getRecord();
                        if (record != null) {
                            result.put(record.getId(), record);
                        }
                    });
                }
            } else if (OdooDataUtils.isCsvFile(file)) {
                OdooDataUtils.processCsvRecord(file, project, (record, lineNumber) -> {
                    result.put(record.getId(), record);
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
    public DataExternalizer<OdooRecord> getValueExternalizer() {
        return new DataExternalizer<OdooRecord>() {
            @Override
            public void save(@NotNull DataOutput out, OdooRecord value) throws IOException {
                out.writeUTF(value.getName());
                out.writeUTF(StringUtil.notNullize(value.getModel()));
                out.writeUTF(value.getModule());
                out.writeUTF(Optional.ofNullable(value.getSubType()).map(Enum::name).orElse(""));
            }

            @Override
            public OdooRecord read(@NotNull DataInput in) throws IOException {
                String name = in.readUTF();
                String model = in.readUTF();
                String module = in.readUTF();
                OdooRecordSubType type = null;
                try {
                    type = OdooRecordSubType.valueOf(in.readUTF());
                } catch (IllegalArgumentException ignored) {
                }
                return new OdooRecordImpl(name, model, module, type, null);
            }
        };
    }

    @Override
    public int getVersion() {
        return 8;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> {
            return OdooDataUtils.isCsvFile(file) || OdooDataUtils.isXmlFile(file);
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @NotNull
    public static Collection<String> getAllIds(@NotNull GlobalSearchScope scope) {
        return Stream.concat(getIds(scope).stream(), getImplicitIds(scope).stream()).collect(Collectors.toSet());
    }

    @NotNull
    private static Collection<String> getIds(@NotNull GlobalSearchScope scope) {
        Set<String> ids = new HashSet<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        index.processAllKeys(NAME, ids::add, scope, null);
        return ids;
    }

    @NotNull
    private static Collection<String> getImplicitIds(@NotNull GlobalSearchScope scope) {
        Set<String> ids = new HashSet<>();
        processImplicitRecords(scope, record -> {
            ids.add(record.getId());
            return true;
        });
        return ids;
    }

    private static boolean processImplicitRecords(@NotNull GlobalSearchScope scope, @NotNull Processor<OdooRecord> processor) {
        return processIrModelRecords(scope, processor);
    }

    private static boolean processIrModelRecords(@NotNull GlobalSearchScope scope, @NotNull Processor<OdooRecord> processor) {
        Project project = scope.getProject();
        if (project == null) {
            return true;
        }
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<String> models = index.getAllKeys(OdooModelIndex.NAME, project);
        for (String model : models) {
            if (!index.processValues(OdooModelIndex.NAME, model, null, (file, value) -> {
                OdooModule module = OdooModule.findModule(file, project);
                if (module != null) {
                    String name = "model_" + model.replace(".", "_");
                    return processor.process(new OdooRecordImpl(name, OdooNames.IR_MODEL, module.getName(), null, file));
                }
                return true;
            }, scope)) {
                return false;
            }
        }
        return true;
    }

    private static boolean processRecordsByIds(@NotNull GlobalSearchScope scope,
                                               @NotNull Processor<OdooRecord> processor,
                                               @NotNull Collection<String> ids) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        for (String id : ids) {
            if (!index.processValues(NAME, id, null, (file, value) -> {
                value = new OdooRecordImpl(value.getName(), value.getModel(), value.getModule(), value.getSubType(), file);
                return processor.process(value);
            }, scope)) {
                return false;
            }
        }
        return true;
    }

    private static boolean processRecords(@NotNull GlobalSearchScope scope, @NotNull Processor<OdooRecord> processor) {
        Collection<String> ids = getIds(scope);
        return processRecordsByIds(scope, processor, ids);
    }

    public static boolean processAllRecords(@NotNull GlobalSearchScope scope, @NotNull Processor<OdooRecord> processor) {
        if (!processRecords(scope, processor)) {
            return false;
        }
        return processImplicitRecords(scope, processor);
    }

    @NotNull
    public static List<OdooRecord> getAvailableRecords(@NotNull PsiElement anchor) {
        OdooModule module = OdooModule.findModule(anchor);
        if (module != null) {
            GlobalSearchScope scope = module.getSearchScope();
            return getAllRecords(scope);
        }
        return Collections.emptyList();
    }

    @NotNull
    public static List<OdooRecord> getAllRecords(@NotNull GlobalSearchScope scope) {
        List<OdooRecord> records = new LinkedList<>();
        processAllRecords(scope, records::add);
        return records;
    }

    @NotNull
    public static List<OdooRecord> findRecordsById(@NotNull String id, @NotNull PsiElement anchor) {
        OdooModule odooModule = OdooModule.findModule(anchor);
        if (odooModule != null) {
            return findRecordsById(id, odooModule.getSearchScope());
        }
        Module module = ModuleUtil.findModuleForPsiElement(anchor);
        if (module != null) {
            return findRecordsById(id, module.getModuleContentWithDependenciesScope());
        }
        return Collections.emptyList();
    }

    @NotNull
    public static List<OdooRecord> findRecordsById(@NotNull String id, @NotNull GlobalSearchScope scope) {
        List<OdooRecord> records = new LinkedList<>();
        processRecordsByIds(scope, records::add, Collections.singleton(id));
        processImplicitRecords(scope, record -> {
            if (id.equals(record.getId())) {
                records.add(record);
            }
            return true;
        });
        return records;
    }
}
