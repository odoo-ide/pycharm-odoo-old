package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.EverythingGlobalScope;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.Processor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
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
    private static final OdooRecordCache RECORD_CACHE = new OdooRecordCache();

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
                            result.put(record.getId(), record.withoutDataFile());
                            RECORD_CACHE.add(record);
                        }
                    });
                }
            } else if (OdooDataUtils.isCsvFile(file)) {
                OdooDataUtils.processCsvRecord(file, project, (record, lineNumber) -> {
                    result.put(record.getId(), record.withoutDataFile());
                    RECORD_CACHE.add(record);
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
    public static Collection<String> getAllIds(@NotNull Project project, @NotNull GlobalSearchScope scope) {
        return Stream.concat(getIds(scope).stream(), getImplicitIds(project, scope).stream()).collect(Collectors.toSet());
    }

    @NotNull
    private static Collection<String> getIds(@NotNull GlobalSearchScope scope) {
        Set<String> ids = new HashSet<>();
        FileBasedIndex index = FileBasedIndex.getInstance();
        index.processAllKeys(NAME, ids::add, scope, null);
        return ids;
    }

    @NotNull
    private static Collection<String> getImplicitIds(@NotNull Project project, @NotNull GlobalSearchScope scope) {
        Set<String> ids = new HashSet<>();
        processImplicitRecords(project, scope, record -> {
            ids.add(record.getId());
            return true;
        });
        return ids;
    }

    private static boolean processImplicitRecords(@NotNull Project project,
                                                  @NotNull GlobalSearchScope scope,
                                                  @NotNull Processor<OdooRecord> processor) {
        return OdooModelIndex.processIrModelRecords(project, scope, processor);
    }

    private static boolean processRecordsByIds(@NotNull Project project,
                                               @NotNull GlobalSearchScope scope,
                                               @NotNull Processor<OdooRecord> processor,
                                               @NotNull Collection<String> ids) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        GlobalSearchScope everythingScope = new EverythingGlobalScope(project);
        for (String id : ids) {
            if (!RECORD_CACHE.processRecords(id, processor, scope)) {
                if (!index.processValues(NAME, id, null, (file, value) -> {
                    OdooRecord record = value.withDataFile(file);
                    RECORD_CACHE.add(record);
                    if (scope.contains(file)) {
                        return processor.process(record);
                    }
                    return true;
                }, everythingScope)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean processRecords(@NotNull Project project,
                                          @NotNull GlobalSearchScope scope,
                                          @NotNull Processor<OdooRecord> processor) {
        Collection<String> ids = getIds(scope);
        return processRecordsByIds(project, scope, processor, ids);
    }

    public static boolean processAllRecords(@NotNull Project project,
                                            @NotNull GlobalSearchScope scope,
                                            @NotNull Processor<OdooRecord> processor) {
        if (!processRecords(project, scope, processor)) {
            return false;
        }
        return processImplicitRecords(project, scope, processor);
    }

    @NotNull
    public static List<OdooRecord> findRecordsById(@NotNull String id, @NotNull PsiElement anchor) {
        Project project = anchor.getProject();
        OdooModule odooModule = OdooModule.findModule(anchor);
        if (odooModule != null) {
            return findRecordsById(id, project, odooModule.getSearchScope());
        }
        Module module = ModuleUtil.findModuleForPsiElement(anchor);
        if (module != null) {
            return findRecordsById(id, project, module.getModuleContentWithDependenciesScope());
        }
        return Collections.emptyList();
    }

    @NotNull
    public static List<OdooRecord> findRecordsById(@NotNull String id,
                                                   @NotNull Project project,
                                                   @NotNull GlobalSearchScope scope) {
        List<OdooRecord> records = new LinkedList<>();
        processRecordsByIds(project, scope, records::add, Collections.singleton(id));
        processImplicitRecords(project, scope, record -> {
            if (id.equals(record.getId())) {
                records.add(record);
            }
            return true;
        });
        return records;
    }
}
