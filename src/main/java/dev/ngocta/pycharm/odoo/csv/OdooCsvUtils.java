package dev.ngocta.pycharm.odoo.csv;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairProcessor;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class OdooCsvUtils {
    private OdooCsvUtils() {
    }

    public static boolean isCsvFile(@NotNull VirtualFile file) {
        String extension = file.getExtension();
        return extension != null && "csv".equals(extension.toLowerCase());
    }

    public static void processRecordInCsvFile(@NotNull VirtualFile file,
                                              @NotNull Project project,
                                              @NotNull PairProcessor<OdooRecord, CSVRecord> processor) {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(file, project);
        if (module == null) {
            return;
        }
        try {
            InputStream inputStream = file.getInputStream();
            CSVParser parser = CSVParser.parse(inputStream, file.getCharset(), CSVFormat.DEFAULT.withHeader());
            if (!parser.getHeaderNames().contains("id")) {
                return;
            }
            String model = file.getNameWithoutExtension();
            String moduleName = module.getName();
            for (CSVRecord csvRecord : parser) {
                String id = csvRecord.get("id");
                if (id != null) {
                    OdooRecord record = new OdooRecord(id, model, moduleName, null, file);
                    if (!processor.process(record, csvRecord)) {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
