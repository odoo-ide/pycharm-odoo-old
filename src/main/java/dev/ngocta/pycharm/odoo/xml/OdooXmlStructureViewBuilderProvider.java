package dev.ngocta.pycharm.odoo.xml;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.structureView.impl.xml.XmlStructureViewTreeModel;
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomManager;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRoot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooXmlStructureViewBuilderProvider implements XmlStructureViewBuilderProvider {
    @Nullable
    @Override
    public StructureViewBuilder createStructureViewBuilder(@NotNull XmlFile file) {
        // Own dom is not completed, fallback to default structure view
        if (DomManager.getDomManager(file.getProject()).getFileElement(file, OdooDomRoot.class) != null) {
            return new TreeBasedStructureViewBuilder() {
                @Override
                @NotNull
                public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
                    return new XmlStructureViewTreeModel(file, editor);
                }
            };
        }
        return null;
    }
}
