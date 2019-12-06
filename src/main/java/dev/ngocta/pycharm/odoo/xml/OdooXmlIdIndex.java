package dev.ngocta.pycharm.odoo.xml;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooXmlIdIndex extends ScalarIndexExtension<String> {
    private static final ID<String, Void> NAME = ID.create("odoo.xml.id");

    @NotNull
    @Override
    public ID<String, Void> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> result = new HashMap<>();
            VirtualFile file = inputData.getFile();
            PsiFile psiFile = inputData.getPsiFile();
            if (psiFile instanceof XmlFile) {
                OdooDomRoot root = getDomRoot((XmlFile) psiFile);
                if (root != null) {
                    List<OdooDomRecord> tags = root.getAllRecords();
                    tags.forEach(tag -> {
                        String id = tag.getQualifiedId(file);
                        if (id != null) {
                            result.put(id, null);
                        }
                    });
                }
            } else if ("csv".equals(file.getExtension())) {
//                try {
//                    InputStream inputStream = file.getInputStream();
//                    Reader reader = new InputStreamReader(inputStream);
//                } catch (IOException ignored) {
//                }
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
    public int getVersion() {
        return 1;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> {
            String extension = file.getExtension();
            return "xml".equals(extension) || "csv".equals(extension);
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
    public static Collection<String> getAllXmlIds(@NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        return index.getAllKeys(NAME, project);
    }

    @NotNull
    public static Collection<NavigationItem> findRecordDefinitions(@NotNull String xmlId, @NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<VirtualFile> files = index.getContainingFiles(NAME, xmlId, GlobalSearchScope.allScope(project));
        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<NavigationItem> result = new LinkedList<>();
        files.forEach(file -> {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof XmlFile) {
                OdooDomRoot root = getDomRoot((XmlFile) psiFile);
                if (root != null) {
                    List<OdooDomRecord> records = root.getAllRecords();
                    for (OdooDomRecord record : records) {
                        if (xmlId.equals(record.getQualifiedId(file))) {
                            result.add(new OdooDomRecordNavigation(record));
                        }
                    }
                }
            }
        });
        return result;
    }
}
