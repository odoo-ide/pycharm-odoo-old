package dev.ngocta.pycharm.odoo.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.dom.js.OdooDomJSTemplate;
import dev.ngocta.pycharm.odoo.xml.dom.js.OdooDomJSTemplateFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class OdooJSTemplateIndex extends ScalarIndexExtension<String> {
    public static final @NotNull ID<String, Void> NAME = ID.create("odoo.js.template");

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
            PsiFile file = inputData.getPsiFile();
            OdooDomJSTemplateFile root = OdooXmlUtils.getOdooDomJSTemplateFile(file);
            if (root != null) {
                for (OdooDomJSTemplate template : root.getAllTemplates()) {
                    String name = template.getName();
                    if (name != null) {
                        result.put(name, null);
                    }
                }
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
        return new DefaultFileTypeSpecificInputFilter(XmlFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @NotNull
    public static List<String> getAvailableTemplateNames(@NotNull GlobalSearchScope scope,
                                                         @NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<String> allNames = index.getAllKeys(NAME, project);
        List<String> names = new LinkedList<>();
        for (String name : allNames) {
            index.processValues(NAME, name, null, (file, value) -> {
                names.add(name);
                return false;
            }, scope);
        }
        return names;
    }

    @NotNull
    static List<String> getAvailableTemplateNames(@NotNull PsiElement anchor) {
        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(anchor);
        if (odooModule == null) {
            return Collections.emptyList();
        }
        return getAvailableTemplateNames(odooModule.getOdooModuleWithDependenciesScope(), anchor.getProject());
    }

    @NotNull
    public static List<OdooDomJSTemplate> findTemplatesByName(@NotNull String name,
                                                              @NotNull GlobalSearchScope scope,
                                                              @NotNull Project project) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        List<VirtualFile> files = new LinkedList<>();
        index.processValues(NAME, name, null, (file, value) -> {
            files.add(file);
            return true;
        }, scope);

        PsiManager psiManager = PsiManager.getInstance(project);
        List<OdooDomJSTemplate> templates = new LinkedList<>();
        for (VirtualFile file : files) {
            PsiFile psiFile = psiManager.findFile(file);
            OdooDomJSTemplateFile root = OdooXmlUtils.getOdooDomJSTemplateFile(psiFile);
            if (root != null) {
                for (OdooDomJSTemplate template : root.getAllTemplates()) {
                    if (name.equals(template.getName())) {
                        templates.add(template);
                    }
                }
            }
        }
        return templates;
    }

    @NotNull
    public static List<OdooDomJSTemplate> findTemplatesByName(@NotNull String name,
                                                              @NotNull PsiElement anchor) {
        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(anchor);
        if (odooModule == null) {
            return Collections.emptyList();
        }
        return findTemplatesByName(name, odooModule.getOdooModuleWithDependenciesScope(), anchor.getProject());
    }
}
