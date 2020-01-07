package dev.ngocta.pycharm.odoo.data;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.PairProcessor;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class OdooDataUtils {
    private OdooDataUtils() {
    }

    public static void processCsvRecord(@NotNull VirtualFile file,
                                        @NotNull PairProcessor<OdooRecord, Integer> processor) {
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
            String model = file.getNameWithoutExtension();
            String module = moduleDirectory.getName();
            for (CSVRecord csvRecord : parser) {
                String id = csvRecord.get("id");
                if (id != null) {
                    OdooRecord record = new OdooRecordImpl(id, model, null, module, file);
                    if (!processor.process(record, (int) parser.getCurrentLineNumber())) {
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Nullable
    public static OdooDomRoot getDomRoot(@NotNull XmlFile xmlFile) {
        DomManager domManager = DomManager.getDomManager(xmlFile.getProject());
        DomFileElement<OdooDomRoot> fileElement = domManager.getFileElement(xmlFile, OdooDomRoot.class);
        if (fileElement != null) {
            return fileElement.getRootElement();
        }
        return null;
    }

    public static boolean isCsvFile(@NotNull VirtualFile file) {
        String extension = file.getExtension();
        return extension != null && "csv".equals(extension.toLowerCase());
    }

    public static boolean isXmlFile(@NotNull VirtualFile file) {
        return FileTypeRegistry.getInstance().isFileOfType(file, XmlFileType.INSTANCE);
    }
}
