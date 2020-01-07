package dev.ngocta.pycharm.odoo.data;

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
import dev.ngocta.pycharm.odoo.OdooUtils;
import dev.ngocta.pycharm.odoo.model.OdooModelIndex;
import dev.ngocta.pycharm.odoo.module.OdooModuleIndex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
            PsiFile psiFile = PsiManager.getInstance(inputData.getProject()).findFile(file);
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
                OdooDataUtils.processCsvRecord(file, (record, lineNumber) -> {
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
    public static Collection<String> getAllIds(@NotNull Project project) {
        return Stream.concat(getIds(project).stream(), getImplicitIds(project).stream()).collect(Collectors.toSet());
    }

    @NotNull
    private static Collection<String> getIds(@NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        return index.getAllKeys(NAME, project);
    }

    private static Collection<String> getImplicitIds(@NotNull Project project) {
        Set<String> ids = new HashSet<>();
        processImplicitRecords(project, GlobalSearchScope.allScope(project), record -> ids.add(record.getId()));
        return ids;
    }

    private static void processImplicitRecords(@NotNull Project project, @NotNull GlobalSearchScope scope, @NotNull Processor<OdooRecord> processor) {
        processIrModelRecords(project, scope, processor);
    }

    private static void processIrModelRecords(@NotNull Project project, @NotNull GlobalSearchScope scope, @NotNull Processor<OdooRecord> processor) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<String> models = index.getAllKeys(OdooModelIndex.NAME, project);
        models.forEach(model -> {
            index.processValues(OdooModelIndex.NAME, model, null, (file, value) -> {
                VirtualFile moduleDir = OdooUtils.getOdooModuleDirectory(file);
                if (moduleDir != null) {
                    String name = "model_" + model.replace(".", "_");
                    String module = moduleDir.getName();
                    return processor.process(new OdooRecordImpl(name, OdooNames.IR_MODEL, module, null, file));
                }
                return true;
            }, scope);
        });
    }

    private static void processRecords(@NotNull Project project, @NotNull GlobalSearchScope scope, @NotNull Processor<OdooRecord> processor) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<String> ids = getIds(project);
        for (String id : ids) {
            if (!index.processValues(NAME, id, null, (file, value) -> {
                value = new OdooRecordImpl(value.getName(), value.getModel(), value.getModule(), value.getSubType(), file);
                return processor.process(value);
            }, scope)) {
                return;
            }
        }
    }

    @NotNull
    public static List<OdooRecord> getAvailableRecords(@NotNull PsiElement anchor) {
        Project project = anchor.getProject();
        GlobalSearchScope scope = OdooModuleIndex.getModuleAndDependsScope(anchor);
        List<OdooRecord> records = new LinkedList<>();
        processRecords(project, scope, records::add);
        processImplicitRecords(project, scope, records::add);
        return records;
    }

    @NotNull
    public static List<OdooRecord> findRecordsById(@NotNull String id, @NotNull Project project, @Nullable PsiElement anchor) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        List<OdooRecord> records = new LinkedList<>();
        GlobalSearchScope scope = anchor == null ? GlobalSearchScope.allScope(project) : OdooModuleIndex.getModuleAndDependsScope(anchor);
        index.processValues(NAME, id, null, (file, value) -> {
            records.add(new OdooRecordImpl(value.getName(), value.getModel(), value.getModule(), value.getSubType(), file));
            return true;
        }, scope);
        processImplicitRecords(project, scope, record -> {
            if (record.getId().equals(id)) {
                records.add(record);
            }
            return true;
        });
        return records;
    }
}
