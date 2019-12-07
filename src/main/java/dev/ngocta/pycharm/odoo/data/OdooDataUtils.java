package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.PairProcessor;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

public class OdooDataUtils {
    private OdooDataUtils() {

    }

    public static void processCsvRecord(@NotNull VirtualFile file, @NotNull PairProcessor<String, Integer> processor) {
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
            for (CSVRecord record : parser) {
                String id = record.get("id");
                if (id != null) {
                    if (!id.contains(".")) {
                        id = moduleDirectory.getName() + "." + id;
                    }
                    if (!processor.process(id, (int) parser.getCurrentLineNumber())) {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
}
