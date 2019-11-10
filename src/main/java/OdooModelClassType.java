import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.resolve.RatedResolveResult;
import com.jetbrains.python.psi.types.PyClassLikeType;
import com.jetbrains.python.psi.types.PyClassTypeImpl;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModelClassType extends PyClassTypeImpl {
    private OdooModelInfo myOdooModelInfo;
    private List<PyClassLikeType> cachedAncestorTypes = null;

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
    @Override
    public List<PyClassLikeType> getAncestorTypes(@NotNull TypeEvalContext context) {
        if (cachedAncestorTypes != null) {
            return cachedAncestorTypes;
        }
        List<PyClassLikeType> result = new LinkedList<>();
        getSuperClasses().forEach(pyClass -> {
            OdooModelClassType type = new OdooModelClassType(pyClass, myIsDefinition);
            result.add(type);
            result.addAll(type.getAncestorTypes(context));
        });
        cachedAncestorTypes = result;
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

    @Nullable
    public PsiElement findMember(@NotNull String name, PyResolveContext resolveContext, boolean inherit) {
        TypeEvalContext context = resolveContext.getTypeEvalContext();
        PyTargetExpression attExpr = myClass.findClassAttribute(name, false, context);
        if (attExpr != null) {
            return attExpr;
        }
        PyFunction funcExpr = myClass.findMethodByName(name, false, context);
        if (funcExpr != null) {
            return funcExpr;
        }
        if (inherit) {
            for (PyClassLikeType classType : getAncestorTypes(context)) {
                if (classType instanceof OdooModelClassType) {
                    PsiElement member = ((OdooModelClassType) classType).findMember(name, resolveContext, false);
                    if (member != null) {
                        return member;
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public List<? extends RatedResolveResult> resolveMember(@NotNull String name, @Nullable PyExpression location, @NotNull AccessDirection direction, @NotNull PyResolveContext resolveContext, boolean inherited) {
        if (myOdooModelInfo != null) {
            PsiElement element = findMember(name, resolveContext, inherited);
            if (element != null) {
                return Collections.singletonList(new RatedResolveResult(RatedResolveResult.RATE_NORMAL, element));
            }
        }
        return super.resolveMember(name, location, direction, resolveContext, inherited);
    }
}