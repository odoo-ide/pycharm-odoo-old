package dev.ngocta.pycharm.odoo.data;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.fileTypes.FileTypeRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.patterns.PatternCondition;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.PairProcessor;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import dev.ngocta.pycharm.odoo.module.OdooModuleUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

public class OdooDataUtils {
    public static final PatternCondition<PsiElement> ODOO_XML_ELEMENT_PATTERN_CONDITION =
            new PatternCondition<PsiElement>("odooXmlElement") {
                @Override
                public boolean accepts(@NotNull PsiElement element,
                                       ProcessingContext context) {
                    return inOdooXmlFile(element);
                }
            };

    private OdooDataUtils() {
    }

    public static void processCsvRecord(@NotNull VirtualFile file,
                                        @NotNull Project project,
                                        @NotNull PairProcessor<OdooRecord, Integer> processor) {
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
                    OdooRecord record = new OdooRecordImpl(id, model, null, moduleName, file);
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

    public static boolean isOdooXmlFile(@NotNull PsiFile file) {
        return file instanceof XmlFile && OdooDataUtils.getDomRoot((XmlFile) file) != null;
    }

    public static boolean inOdooXmlFile(@NotNull PsiElement element) {
        PsiFile file = element.getContainingFile();
        return file != null && isOdooXmlFile(file);
    }

    @Nullable
    public static String getViewInheritId(@NotNull OdooDomRecordLike record) {
        if (record instanceof OdooDomRecord) {
            for (OdooDomFieldAssignment field : ((OdooDomRecord) record).getFields()) {
                if ("inherit_id".equals(field.getName().getStringValue())) {
                    return field.getRef().getStringValue();
                }
            }
        } else if (record instanceof OdooDomTemplate) {
            return ((OdooDomTemplate) record).getInheritId().getStringValue();
        }
        return null;
    }
}
