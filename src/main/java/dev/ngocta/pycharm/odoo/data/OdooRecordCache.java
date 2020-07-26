package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OdooRecordCache {
    private final Map<String, Set<OdooRecord>> myCache = new ConcurrentHashMap<>();

    public void add(@NotNull String key,
                    @NotNull OdooRecord record) {
        Set<OdooRecord> cachedRecords = myCache.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet());
        cachedRecords.removeIf(r -> Objects.equals(record.getDataFile(), r.getDataFile()));
        cachedRecords.removeIf(r -> !Objects.equals(record.getModel(), record.getModel()));
        cachedRecords.add(record);
    }

    public void add(@NotNull OdooRecord record) {
        add(record.getQualifiedId(), record);
    }

    public void clearCache(@NotNull String recordId,
                           @Nullable VirtualFile file) {
        if (myCache.containsKey(recordId)) {
            if (file == null) {
                myCache.get(recordId).clear();
            } else {
                myCache.get(recordId).removeIf(record -> file.equals(record.getDataFile()));
            }
        }
    }

    public boolean processRecords(@NotNull String key,
                                  @NotNull Processor<OdooRecord> processor,
                                  @NotNull GlobalSearchScope scope) {
        Set<OdooRecord> records = myCache.getOrDefault(key, new HashSet<>());
        records.forEach(r -> {
            VirtualFile file = r.getDataFile();
            if (file != null && file.isValid() && scope.contains(file)) {
                processor.process(r);
            }
        });
        return !records.isEmpty();
    }
}
