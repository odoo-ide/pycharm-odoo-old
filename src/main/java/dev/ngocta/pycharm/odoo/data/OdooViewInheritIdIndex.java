package dev.ngocta.pycharm.odoo.data;

import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRecordLike;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRoot;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OdooViewInheritIdIndex extends FileBasedIndexExtension<String, String> {
    private static final ID<String, String> NAME = ID.create("odoo.view.inherit.id");

    @Override
    @NotNull
    public ID<String, String> getName() {
        return NAME;
    }

    @Override
    @NotNull
    public DataIndexer<String, String, FileContent> getIndexer() {
        return inputData -> {
            Map<String, String> result = new HashMap<>();
            VirtualFile moduleDirectory = OdooModuleUtils.getContainingOdooModuleDirectory(inputData.getFile());
            if (moduleDirectory == null) {
                return result;
            }
            PsiFile psiFile = PsiManager.getInstance(inputData.getProject()).findFile(inputData.getFile());
            if (!(psiFile instanceof XmlFile)) {
                return result;
            }
            OdooDomRoot root = OdooXmlUtils.getOdooDataDomRoot((XmlFile) psiFile);
            if (root == null) {
                return result;
            }
            List<OdooDomRecordLike> items = root.getAllRecordLikeItems();
            for (OdooDomRecordLike item : items) {
                OdooRecord record = item.getRecord();
                if (record != null && record.getExtraInfo() instanceof OdooRecordViewInfo) {
                    OdooRecordViewInfo info = (OdooRecordViewInfo) record.getExtraInfo();
                    if (info.getInheritId() != null) {
                        String inheritId = info.getInheritId();
                        if (!inheritId.contains(".")) {
                            inheritId = moduleDirectory.getName() + "." + inheritId;
                        }
                        result.put(inheritId, record.getQualifiedId());
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
    @NotNull
    public DataExternalizer<String> getValueExternalizer() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
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

    public static boolean hasOverridingId(@NotNull String inheritId,
                                          @NotNull GlobalSearchScope scope) {
        Ref<Boolean> has = Ref.create(false);
        FileBasedIndex.getInstance().processValues(NAME, inheritId, null, (file, value) -> {
            has.set(true);
            return false;
        }, scope);
        return has.get();
    }

    @NotNull
    public static List<String> getOverridingIds(@NotNull String inheritId,
                                                @NotNull GlobalSearchScope scope) {
        return FileBasedIndex.getInstance().getValues(NAME, inheritId, scope);
    }
}
