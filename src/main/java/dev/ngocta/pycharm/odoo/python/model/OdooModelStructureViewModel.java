package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.ide.util.treeView.smartTree.Sorter;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.structureView.PyStructureViewModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooModelStructureViewModel extends PyStructureViewModel {
    public OdooModelStructureViewModel(@NotNull PsiFile psiFile,
                                       @Nullable Editor editor) {
        super(psiFile, editor, new OdooModelStructureViewElement((PyElement) psiFile));
        withSorters(Sorter.ALPHA_SORTER);
        withSuitableClasses(PyFunction.class, PyClass.class);
    }
}
