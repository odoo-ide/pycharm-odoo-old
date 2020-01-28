package dev.ngocta.pycharm.odoo.module;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.jetbrains.python.PythonFileType;
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
            if (file.getName().equals(OdooModuleUtils.getManifestFileName(inputData.getProject()))) {
                VirtualFile dir = file.getParent();
                if (dir != null) {
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
    public static OdooModule getModule(@NotNull String moduleName, @NotNull Project project) {
        FileBasedIndex fileIndex = FileBasedIndex.getInstance();
        Collection<VirtualFile> files = fileIndex.getContainingFiles(NAME, moduleName, GlobalSearchScope.allScope(project));
        for (VirtualFile file : files) {
            return OdooModule.findModule(file, project);
        }
        return null;
    }

    @NotNull
    public static List<OdooModule> getAllModules(@NotNull Project project) {
        List<OdooModule> modules = new LinkedList<>();
        Collection<String> moduleNames = FileBasedIndex.getInstance().getAllKeys(NAME, project);
        for (String name : moduleNames) {
            OdooModule module = getModule(name, project);
            if (module != null) {
                modules.add(module);
            }
        }
        return modules;
    }
}
