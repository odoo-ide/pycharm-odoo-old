package dev.ngocta.pycharm.odoo.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.data.OdooRecordViewInfo;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomDataFile;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomJSTemplate;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomJSTemplateFile;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRecordLike;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdooViewInheritIdIndex extends ScalarIndexExtension<String> {
    private static final ID<String, Void> NAME = ID.create("odoo.view.inherit.id");

    @Override
    @NotNull
    public ID<String, Void> getName() {
        return NAME;
    }

    @Override
    @NotNull
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> result = new HashMap<>();
            PsiFile psiFile = PsiManager.getInstance(inputData.getProject()).findFile(inputData.getFile());
            OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(psiFile);
            if (odooModule == null) {
                return result;
            }
            OdooDomDataFile dataFile = OdooXmlUtils.getOdooDataDomFile(psiFile);
            if (dataFile != null) {
                List<OdooDomRecordLike> items = dataFile.getAllRecordLikeItems();
                for (OdooDomRecordLike item : items) {
                    OdooRecord record = item.getRecord();
                    if (record != null && record.getExtraInfo() instanceof OdooRecordViewInfo) {
                        OdooRecordViewInfo info = (OdooRecordViewInfo) record.getExtraInfo();
                        if (info.getInheritId() != null) {
                            String inheritId = info.getInheritId();
                            if (!inheritId.contains(".")) {
                                inheritId = odooModule.getName() + "." + inheritId;
                            }
                            result.put(inheritId, null);
                        }
                    }
                }
            }

            OdooDomJSTemplateFile templateFile = OdooXmlUtils.getOdooDomJSTemplateFile(psiFile);
            if (templateFile != null) {
                for (OdooDomJSTemplate template : templateFile.getAllTemplates()) {
                    String inherit = template.getInheritName();
                    if (inherit != null) {
                        result.put(inherit, null);
                    }
                }
            }
            return result;
        };
    }

    @Override
    @NotNull
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    @NotNull
    public FileBasedIndex.InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(XmlFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    public static boolean processChildrenViewRecords(@NotNull String parentViewId,
                                                     @NotNull Processor<OdooDomRecordLike> processor,
                                                     @NotNull GlobalSearchScope scope,
                                                     @NotNull Project project) {
        Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(NAME, parentViewId, scope);
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile file : files) {
            PsiFile psiFile = psiManager.findFile(file);
            OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(psiFile);
            if (odooModule == null) {
                continue;
            }
            OdooDomDataFile dataFile = OdooXmlUtils.getDomFile(psiFile, OdooDomDataFile.class);
            if (dataFile == null) {
                continue;
            }
            for (OdooDomRecordLike domRecord : dataFile.getAllRecordLikeItems()) {
                if (domRecord.getRecordExtraInfo() instanceof OdooRecordViewInfo) {
                    String inheritId = ((OdooRecordViewInfo) domRecord.getRecordExtraInfo()).getInheritId();
                    if (inheritId == null) {
                        continue;
                    }
                    if (!inheritId.contains(".")) {
                        inheritId = odooModule.getName() + "." + inheritId;
                    }
                    if (parentViewId.equals(inheritId)) {
                        if (!processor.process(domRecord)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean processChildrenJSTemplates(@NotNull String parentTemplateName,
                                                     @NotNull Processor<OdooDomJSTemplate> processor,
                                                     @NotNull GlobalSearchScope scope,
                                                     @NotNull Project project) {
        Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(NAME, parentTemplateName, scope);
        PsiManager psiManager = PsiManager.getInstance(project);
        for (VirtualFile file : files) {
            PsiFile psiFile = psiManager.findFile(file);
            OdooDomJSTemplateFile templateFile = OdooXmlUtils.getDomFile(psiFile, OdooDomJSTemplateFile.class);
            if (templateFile == null) {
                continue;
            }
            for (OdooDomJSTemplate template : templateFile.getAllTemplates()) {
                if (parentTemplateName.equals(template.getInheritName())) {
                    if (!processor.process(template)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static boolean processChildrenJSTemplates(@NotNull OdooDomJSTemplate parentTemplate,
                                                     @NotNull Processor<OdooDomJSTemplate> processor,
                                                     @NotNull GlobalSearchScope scope,
                                                     @NotNull Project project) {
        String name = parentTemplate.getName();
        if (name == null) {
            return true;
        }
        if (!processChildrenJSTemplates(name, processor, scope, project)) {
            return false;
        }
        String qualifiedName = parentTemplate.getQualifiedName();
        if (qualifiedName != null && !qualifiedName.equals(name)) {
            return processChildrenJSTemplates(qualifiedName, processor, scope, project);
        }
        return true;
    }
}
