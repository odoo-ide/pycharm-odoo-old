package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyFile;
import dev.ngocta.pycharm.odoo.model.OdooModelInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OdooRecordImpl implements OdooRecord {
    private final String myName;
    private final String myModel;
    private final String myModule;
    private final OdooRecordSubType mySubType;
    private final VirtualFile myDataFile;

    public OdooRecordImpl(@NotNull String name,
                          @Nullable String model,
                          @NotNull String module,
                          @Nullable OdooRecordSubType subType,
                          @Nullable VirtualFile dataFile) {
        myName = name;
        myModel = model;
        myModule = module;
        mySubType = subType;
        myDataFile = dataFile;
    }

    public OdooRecordImpl(@NotNull String id,
                          @Nullable String model,
                          @Nullable OdooRecordSubType subType,
                          @NotNull String containingModule,
                          @Nullable VirtualFile dataFile) {
        String name, module;
        String[] splits = id.split("\\.", 2);
        if (splits.length == 1) {
            name = splits[0];
            module = containingModule;
        } else {
            name = splits[1];
            module = splits[0];
        }
        myName = name;
        myModel = model;
        myModule = module;
        mySubType = subType;
        myDataFile = dataFile;
    }

    @NotNull
    public String getName() {
        return myName;
    }

    @Override
    public String getModel() {
        return myModel;
    }

    @NotNull
    public String getModule() {
        return myModule;
    }

    @NotNull
    public String getId() {
        return getModule() + "." + getName();
    }

    @Nullable
    public OdooRecordSubType getSubType() {
        return mySubType;
    }

    @Nullable
    @Override
    public VirtualFile getDataFile() {
        return myDataFile;
    }

    @Override
    public List<PsiElement> getElements(@NotNull Project project) {
        if (myDataFile != null) {
            if (OdooDataUtils.isCsvFile(myDataFile)) {
                return Collections.singletonList(new OdooCsvRecord(myDataFile, project, getId()));
            } else {
                PsiFile file = PsiManager.getInstance(project).findFile(myDataFile);
                if (file instanceof XmlFile) {
                    OdooDomRoot root = OdooDataUtils.getDomRoot((XmlFile) file);
                    if (root != null) {
                        return root.getAllRecordLikeItems().stream()
                                .filter(record -> this.equals(record.getRecord()))
                                .map(OdooDomRecordLike::getXmlElement)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
                    }
                } else if (file instanceof PyFile) {
                    List<PyClass> classes = ((PyFile) file).getTopLevelClasses();
                    return classes.stream()
                            .filter(cls -> {
                                OdooModelInfo info = OdooModelInfo.getInfo(cls);
                                return info != null && info.getName().replace(".", "_").equals(myName.substring(6));
                            })
                            .collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }

    public List<NavigationItem> getNavigationItems(@NotNull Project project) {
        return getElements(project).stream()
                .map(element -> {
                    if (element instanceof PyElement) {
                        return (PyElement) element;
                    } else {
                        return new OdooRecordNavigationItem(this, element);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooRecordImpl that = (OdooRecordImpl) o;
        return Objects.equals(myName, that.myName) &&
                Objects.equals(myModel, that.myModel) &&
                Objects.equals(myModule, that.myModule) &&
                Objects.equals(mySubType, that.mySubType) &&
                Objects.equals(myDataFile, that.myDataFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myName, myModel, myModule, mySubType, myDataFile);
    }

    public OdooRecord withDataFile(VirtualFile file) {
        return new OdooRecordImpl(myName, myModel, myModule, mySubType, file);
    }

    public OdooRecord withoutDataFile() {
        return new OdooRecordImpl(myName, myModel, myModule, mySubType, null);
    }
}
