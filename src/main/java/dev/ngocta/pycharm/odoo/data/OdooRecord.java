package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.csv.OdooCsvRecord;
import dev.ngocta.pycharm.odoo.csv.OdooCsvUtils;
import dev.ngocta.pycharm.odoo.python.model.OdooModelInfo;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRecordLike;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRoot;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OdooRecord {
    private final String myId;
    private final String myModel;
    private final String myModule;
    private final String myQualifiedId;
    private final String myUnqualifiedId;
    private final String myOriginModule;
    private final OdooRecordExtraInfo myExtraInfo;
    private final VirtualFile myDataFile;

    public OdooRecord(@NotNull String id,
                      @NotNull String model,
                      @NotNull String module,
                      @Nullable OdooRecordExtraInfo extraInfo,
                      @Nullable VirtualFile dataFile) {
        myId = id;
        myModel = model;
        myModule = module;
        if (id.contains(".")) {
            String[] splits = id.split("\\.", 2);
            myQualifiedId = id;
            myUnqualifiedId = splits[1];
            myOriginModule = splits[0];
        } else {
            myQualifiedId = module + "." + id;
            myUnqualifiedId = id;
            myOriginModule = module;
        }
        myExtraInfo = extraInfo;
        myDataFile = dataFile;
    }

    @NotNull
    public String getModel() {
        return myModel;
    }

    @NotNull
    public String getModule() {
        return myModule;
    }

    @NotNull
    public String getId() {
        return myId;
    }

    @NotNull
    public String getQualifiedId() {
        return myQualifiedId;
    }

    @NotNull
    String getUnqualifiedId() {
        return myUnqualifiedId;
    }

    @NotNull
    String getOriginModule() {
        return myOriginModule;
    }

    @Nullable
    public OdooRecordExtraInfo getExtraInfo() {
        return myExtraInfo;
    }

    @Nullable
    public VirtualFile getDataFile() {
        return myDataFile;
    }

    public List<PsiElement> getElements(@NotNull Project project) {
        if (myDataFile == null || !myDataFile.isValid()) {
            return Collections.emptyList();
        }
        if (OdooCsvUtils.isCsvFile(myDataFile)) {
            return Collections.singletonList(new OdooCsvRecord(myDataFile, project, myId));
        }
        PsiFile file = PsiManager.getInstance(project).findFile(myDataFile);
        if (file == null) {
            return Collections.emptyList();
        }
        List<PsiElement> result = PyUtil.getParameterizedCachedValue(file, this, param -> {
            List<PsiElement> elements = new LinkedList<>();
            if (file instanceof XmlFile) {
                OdooDomRoot root = OdooXmlUtils.getOdooDataDomRoot((XmlFile) file);
                if (root != null) {
                    List<OdooDomRecordLike> records = root.getAllRecordLikeItems();
                    for (OdooDomRecordLike record : records) {
                        if (this.equals(record.getRecord())) {
                            XmlElement xmlElement = record.getXmlElement();
                            if (xmlElement != null) {
                                elements.add(xmlElement);
                            }
                        }
                    }
                }
            } else if (file instanceof PyFile) {
                List<PyClass> classes = ((PyFile) file).getTopLevelClasses();
                for (PyClass cls : classes) {
                    OdooModelInfo info = OdooModelInfo.getInfo(cls);
                    if (info != null && OdooModelUtils.getIrModelRecordId(info.getName()).equals(myId)) {
                        elements.add(cls);
                    }
                }
            }
            return elements;
        });
        return Collections.unmodifiableList(result);
    }

    public List<NavigatablePsiElement> getNavigableElements(@NotNull Project project) {
        return getElements(project).stream()
                .map(element -> {
                    if (element instanceof PyElement) {
                        return (PyElement) element;
                    } else {
                        return new OdooRecordNavigableElement(this, element);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooRecord that = (OdooRecord) o;
        return myId.equals(that.myId) &&
                myModel.equals(that.myModel) &&
                myModule.equals(that.myModule) &&
                Objects.equals(myExtraInfo, that.myExtraInfo) &&
                Objects.equals(myDataFile, that.myDataFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myId, myModel, myModule, myExtraInfo, myDataFile);
    }

    @NotNull
    public OdooRecord withDataFile(@Nullable VirtualFile file) {
        if (Objects.equals(myDataFile, file)) {
            return this;
        }
        return new OdooRecord(myId, myModel, myModule, myExtraInfo, file);
    }

    @NotNull
    public OdooRecord withoutDataFile() {
        return withDataFile(null);
    }
}
