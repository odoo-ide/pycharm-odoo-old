package dev.ngocta.pycharm.odoo.python.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.PythonFileType;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModuleIndex extends ScalarIndexExtension<String> {
    public static final @NotNull ID<String, Void> NAME = ID.create("odoo.module");

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
            if (OdooNames.MANIFEST_FILE_NAME.equals(file.getName())) {
                VirtualFile dir = file.getParent();
                if (OdooModuleUtils.isOdooModuleDirectory(dir)) {
                    result.put(dir.getName(), null);
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
        return new DefaultFileTypeSpecificInputFilter(PythonFileType.INSTANCE);
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @Nullable
    public static OdooModule getOdooModuleByName(@NotNull String moduleName,
                                                 @NotNull Project project,
                                                 @NotNull GlobalSearchScope scope) {
        FileBasedIndex fileIndex = FileBasedIndex.getInstance();
        Collection<VirtualFile> files = fileIndex.getContainingFiles(NAME, moduleName, scope);
        for (VirtualFile file : files) {
            OdooModule module = OdooModuleUtils.getContainingOdooModule(file, project);
            if (module != null) {
                return module;
            }
        }
        return null;
    }

    @Nullable
    public static OdooModule getOdooModuleByName(@NotNull String moduleName,
                                                 @NotNull PsiElement anchor) {
        return getOdooModuleByName(moduleName, anchor.getProject(), OdooUtils.getProjectModuleAndDependenciesScope(anchor));
    }

    @NotNull
    public static List<OdooModule> getAllOdooModules(@NotNull Project project,
                                                     @NotNull GlobalSearchScope scope) {
        List<OdooModule> modules = new LinkedList<>();
        Collection<String> moduleNames = FileBasedIndex.getInstance().getAllKeys(NAME, project);
        for (String name : moduleNames) {
            OdooModule module = getOdooModuleByName(name, project, scope);
            if (module != null) {
                modules.add(module);
            }
        }
        return modules;
    }

    @NotNull
    public static List<OdooModule> getAvailableOdooModules(@NotNull PsiElement anchor) {
        return getAllOdooModules(anchor.getProject(), OdooUtils.getProjectModuleAndDependenciesScope(anchor));
    }
}
