package dev.ngocta.pycharm.odoo.model;

import com.google.common.collect.Lists;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElementVisitor;
import com.jetbrains.python.psi.PyFile;
import dev.ngocta.pycharm.odoo.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OdooModelInheritIndex extends FileBasedIndexExtension<String, Void> {
    public static final @NotNull ID<String, Void> NAME = ID.create("odoo.model.inherit");

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
            VirtualFile virtualFile = inputData.getFile();
            if (OdooModuleUtils.isInOdooModule(virtualFile)) {
                inputData.getPsiFile().acceptChildren(new PyElementVisitor() {
                    @Override
                    public void visitPyClass(PyClass node) {
                        super.visitPyClass(node);
                        OdooModelInfo info = OdooModelInfo.getInfo(node);
                        if (info != null) {
                            for (String s : info.getInherit()) {
                                if (!s.equals(info.getName())) {
                                    result.putIfAbsent(s, null);
                                }
                            }
                        }
                    }
                });
            }
            return result;
        };
    }

    @NotNull
    @Override
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @NotNull
    @Override
    public DataExternalizer<Void> getValueExternalizer() {
        return VoidDataExternalizer.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 2;
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
    private static List<PyClass> getOdooModelClassesByInheritModelInFile(@NotNull String inheritModel,
                                                                         @NotNull PyFile file) {
        List<PyClass> result = new LinkedList<>();
        List<PyClass> classes = file.getTopLevelClasses();
        Lists.reverse(classes).forEach(cls -> {
            OdooModelInfo info = OdooModelInfo.getInfo(cls);
            if (info != null && info.getInherit().contains(inheritModel)) {
                result.add(cls);
            }
        });
        return result;
    }

    @NotNull
    public static List<PyClass> getOdooModelClassesByInheritModel(@NotNull String inheritModel,
                                                                  @NotNull Project project,
                                                                  @NotNull GlobalSearchScope scope) {
        List<PyClass> result = new LinkedList<>();
        Collection<VirtualFile> files = FileBasedIndex.getInstance().getContainingFiles(NAME, inheritModel, scope);
        PsiManager psiManager = PsiManager.getInstance(project);
        files.forEach(file -> {
            PsiFile psiFile = psiManager.findFile(file);
            if (psiFile instanceof PyFile) {
                List<PyClass> classes = getOdooModelClassesByInheritModelInFile(inheritModel, (PyFile) psiFile);
                result.addAll(classes);
            }
        });
        return result;
    }
}
