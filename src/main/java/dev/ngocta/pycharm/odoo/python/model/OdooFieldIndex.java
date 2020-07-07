package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import com.intellij.util.io.VoidDataExternalizer;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElementVisitor;
import com.jetbrains.python.psi.PyTargetExpression;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooFieldIndex extends FileBasedIndexExtension<String, Void> {
    public static final ID<String, Void> NAME = ID.create("odoo.field");

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
            inputData.getPsiFile().acceptChildren(new PyElementVisitor() {
                @Override
                public void visitPyClass(PyClass cls) {
                    super.visitPyClass(cls);
                    OdooModelInfo clsInfo = OdooModelInfo.getInfo(cls);
                    if (clsInfo != null) {
                        List<PyTargetExpression> attributes = cls.getClassAttributes();
                        for (PyTargetExpression attribute : attributes) {
                            OdooFieldInfo info = OdooFieldInfo.getInfo(attribute);
                            if (info != null && attribute.getName() != null) {
                                result.put(attribute.getName(), null);
                            }
                        }
                    }
                }
            });
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
        return new OdooModelInputFilter();
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    @NotNull
    public static Collection<String> getAllFieldNames(@NotNull Project project,
                                                      @Nullable GlobalSearchScope scope) {
        if (scope == null) {
            scope = GlobalSearchScope.projectScope(project);
        }
        Collection<String> names = new LinkedList<>();
        FileBasedIndex.getInstance().processAllKeys(NAME, s -> {
            names.add(s);
            return true;
        }, scope, null);
        return names;
    }

    @NotNull
    public static Collection<String> getAvailableFieldNames(@NotNull PsiElement anchor) {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(anchor);
        if (module != null) {
            return getAllFieldNames(anchor.getProject(), module.getSearchScope());
        }
        return Collections.emptyList();
    }
}
