package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.PythonFileType;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OdooModuleDependIndex extends ScalarIndexExtension<String> {
    public static final @NotNull ID<String, Void> NAME = ID.create("odoo.module.depend");

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
            if (OdooNames.MANIFEST_FILE_NAME.equals(file.getName())) {
                OdooManifestInfo info = OdooManifestInfo.parseManifest(file);
                if (info != null && info.getDepends() != null) {
                    for (String depend : info.getDepends()) {
                        result.put(depend, null);
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
        return 0;
    }

    @NotNull
    @Override
    public FileBasedIndex.InputFilter getInputFilter() {
        return new DefaultFileTypeSpecificInputFilter(PythonFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @NotNull
    public static List<OdooModule> getDependingOdooModules(@NotNull String dependedModuleName,
                                                           @NotNull Project project,
                                                           @NotNull GlobalSearchScope scope) {
        List<OdooModule> result = new LinkedList<>();
        FileBasedIndex fileIndex = FileBasedIndex.getInstance();
        Collection<VirtualFile> files = fileIndex.getContainingFiles(NAME, dependedModuleName, scope);
        for (VirtualFile file : files) {
            OdooModule module = OdooModuleUtils.getContainingOdooModule(file, project);
            if (module != null) {
                result.add(module);
            }
        }
        return result;
    }

    @NotNull
    public static List<OdooModule> getDependingOdooModules(@NotNull String dependedModuleName,
                                                           @NotNull PsiElement anchor) {
        return getDependingOdooModules(
                dependedModuleName, anchor.getProject(),
                OdooUtils.getProjectModuleWithDependenciesScope(anchor));
    }
}
