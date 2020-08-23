package dev.ngocta.pycharm.odoo.xml;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorIntegerDescriptor;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomJSTemplate;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomJSTemplateFile;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class OdooJSTemplateIndex extends FileBasedIndexExtension<String, Integer> {
    public static final @NotNull ID<String, Integer> NAME = ID.create("odoo.js.template");
    public static final int VALUE_IS_NAME = 1;
    public static final int VALUE_IS_QUALIFIED_NAME = 2;
    public static final int VALUE_IS_NAME_AND_QUALIFIED_NAME = 3;

    @NotNull
    @Override
    public ID<String, Integer> getName() {
        return NAME;
    }

    @NotNull
    @Override
    public DataIndexer<String, Integer, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Integer> result = new HashMap<>();
            PsiFile file = PsiManager.getInstance(inputData.getProject()).findFile(inputData.getFile());
            OdooDomJSTemplateFile templateFile = OdooXmlUtils.getOdooDomJSTemplateFile(file);
            if (templateFile == null) {
                return result;
            }
            for (OdooDomJSTemplate template : templateFile.getAllTemplates()) {
                String name = template.getName();
                String qualifiedName = template.getQualifiedName();
                if (name != null && qualifiedName != null) {
                    if (name.equals(qualifiedName)) {
                        result.put(name, VALUE_IS_NAME_AND_QUALIFIED_NAME);
                    } else {
                        result.put(name, VALUE_IS_NAME);
                        result.put(qualifiedName, VALUE_IS_QUALIFIED_NAME);
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
    @NotNull
    public DataExternalizer<Integer> getValueExternalizer() {
        return EnumeratorIntegerDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 3;
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

    public static boolean isRightValue(@NotNull Integer value,
                                       boolean isQualified) {
        return (isQualified && (value & VALUE_IS_QUALIFIED_NAME) != 0)
                || (!isQualified && (value & VALUE_IS_NAME) != 0);
    }

    public static void processAvailableTemplateNames(@NotNull GlobalSearchScope scope,
                                                     @NotNull Project project,
                                                     @NotNull Processor<String> processor,
                                                     boolean isQualified) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<String> allNames = index.getAllKeys(NAME, project);
        for (String name : allNames) {
            index.processValues(NAME, name, null, (file, value) -> {
                if (isRightValue(value, isQualified)) {
                    processor.process(name);
                }
                return false;
            }, scope);
        }
    }

    @NotNull
    public static List<String> getAvailableTemplateNames(@NotNull GlobalSearchScope scope,
                                                         @NotNull Project project,
                                                         boolean isQualified) {
        List<String> names = new LinkedList<>();
        processAvailableTemplateNames(scope, project, names::add, isQualified);
        return names;
    }

    @NotNull
    static List<String> getAvailableTemplateNames(@NotNull PsiElement anchor,
                                                  boolean isQualified) {
        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(anchor);
        if (odooModule == null) {
            return Collections.emptyList();
        }
        return getAvailableTemplateNames(odooModule.getOdooModuleWithDependenciesScope(), anchor.getProject(), isQualified);
    }

    @NotNull
    public static List<OdooDomJSTemplate> findTemplatesByName(@NotNull String name,
                                                              @NotNull GlobalSearchScope scope,
                                                              @NotNull Project project,
                                                              boolean isQualified) {
        List<VirtualFile> files = new LinkedList<>();
        FileBasedIndex.getInstance().processValues(NAME, name, null, (file, value) -> {
            if (isRightValue(value, isQualified))
                files.add(file);
            return true;
        }, scope);

        PsiManager psiManager = PsiManager.getInstance(project);
        List<OdooDomJSTemplate> templates = new LinkedList<>();
        for (VirtualFile file : files) {
            PsiFile psiFile = psiManager.findFile(file);
            OdooDomJSTemplateFile templateFile = OdooXmlUtils.getOdooDomJSTemplateFile(psiFile);
            if (templateFile != null) {
                for (OdooDomJSTemplate template : templateFile.getAllTemplates()) {
                    if (template.isTemplateOf(name, isQualified)) {
                        templates.add(template);
                    }
                }
            }
        }
        return templates;
    }

    @NotNull
    public static List<OdooDomJSTemplate> findTemplatesByName(@NotNull String name,
                                                              @NotNull PsiElement anchor,
                                                              boolean isQualified) {
        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(anchor);
        if (odooModule == null) {
            return Collections.emptyList();
        }
        return findTemplatesByName(name, odooModule.getOdooModuleWithDependenciesScope(), anchor.getProject(), isQualified);
    }
}
