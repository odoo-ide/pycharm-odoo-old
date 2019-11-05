import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.types.PyClassLikeType;
import com.jetbrains.python.psi.types.PyClassTypeImpl;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class OdooModelClassType extends PyClassTypeImpl {
    private OdooModelInfo myOdooModelInfo;

    public OdooModelClassType(@NotNull PyClass source, boolean isDefinition) {
        super(source, isDefinition);
        myOdooModelInfo = OdooModelIndex.getModelInfo(source);
    }

    @NotNull
    @Override
    public List<PyClassLikeType> getSuperClassTypes(@NotNull TypeEvalContext context) {
        if (myOdooModelInfo == null || myOdooModelInfo.getInherit().isEmpty()) {
            return super.getSuperClassTypes(context);
        }
        List<PyClassLikeType> result = new LinkedList<>();
        List<PyClass> supers = getSuperClasses();
        supers.forEach(pyClass -> {
            result.add(new OdooModelClassType(pyClass, myIsDefinition));
        });
        return result;
    }

    @NotNull
    private List<PyClass> getSuperClasses() {
        List<PyClass> result = new LinkedList<>();
        if (myOdooModelInfo != null) {
            myOdooModelInfo.getInherit().forEach(s -> {
                resolveSuperClasses(s, myOdooModelInfo.getModuleName(), result);
            });
        }
        return result;
    }

    private void resolveSuperClasses(String model, String moduleName, List<PyClass> result) {
        Project project = myClass.getProject();
        PsiDirectory module = OdooModuleIndex.getModuleByName(moduleName, project);
        if (module == null) {
            return;
        }
        List<PyClass> pyClasses = OdooModelIndex.findModelClasses(model, module);
        pyClasses.remove(myClass);
        if (pyClasses.isEmpty()) {
            List<String> depends = OdooModuleIndex.getDepends(moduleName, project);
            depends.forEach(depend -> resolveSuperClasses(model, depend, result));
        } else {
            for (PyClass pyClass : pyClasses) {
                if (result.contains(pyClass)) {
                    return;
                }
            }
            result.addAll(pyClasses);
        }
    }
}