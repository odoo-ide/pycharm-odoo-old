package dev.ngocta.pycharm.odoo.data;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomTarget;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.csv.OdooCsvRecord;
import dev.ngocta.pycharm.odoo.csv.OdooCsvUtils;
import dev.ngocta.pycharm.odoo.python.model.OdooModelInfo;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.OdooXmlUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomDataFile;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRecordLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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
                OdooDomDataFile root = OdooXmlUtils.getOdooDataDomFile(file);
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
                if (OdooNames.IR_MODULE_MODULE.equals(myModel)) {
                    OdooModule module = OdooModuleUtils.getContainingOdooModule(file);
                    if (module != null) {
                        elements.add(module.getDirectory());
                    }
                } else if (OdooNames.IR_MODEL.equals(myModel) && myExtraInfo instanceof OdooRecordModelInfo) {
                    String modelName = ((OdooRecordModelInfo) myExtraInfo).getModelName();
                    List<PyClass> classes = ((PyFile) file).getTopLevelClasses();
                    for (PyClass cls : classes) {
                        OdooModelInfo info = OdooModelInfo.getInfo(cls);
                        if (info != null && info.getName().equals(modelName)) {
                            elements.add(cls);
                        }
                    }
                } else if (OdooNames.IR_MODEL_FIELDS.equals(myModel) && myExtraInfo instanceof OdooRecordFieldInfo) {
                    String fieldName = ((OdooRecordFieldInfo) myExtraInfo).getFieldName();
                    String modelName = ((OdooRecordFieldInfo) myExtraInfo).getModelName();
                    List<PyClass> classes = ((PyFile) file).getTopLevelClasses();
                    for (PyClass cls : classes) {
                        OdooModelInfo info = OdooModelInfo.getInfo(cls);
                        if (info != null && info.getName().equals(modelName)) {
                            PyTargetExpression field = cls.findClassAttribute(fieldName, false, null);
                            if (field != null) {
                                elements.add(field);
                            }
                        }
                    }
                }
            }
            return elements;
        });
        return Collections.unmodifiableList(result);
    }

    public List<OdooRecordElement> getRecordElements(@NotNull Project project) {
        List<OdooRecordElement> recordElements = new LinkedList<>();
        List<PsiElement> elements = getElements(project);
        for (PsiElement element : elements) {
            PsiTarget target = null;
            if (element instanceof XmlTag) {
                DomElement domElement = DomManager.getDomManager(project).getDomElement((XmlTag) element);
                if (domElement != null) {
                    target = DomTarget.getTarget(domElement);
                }
            }
            if (target == null) {
                target = new DelegatePsiTarget(element);
            }
            recordElements.add(new OdooRecordElement(this, target));
        }
        return recordElements;
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
