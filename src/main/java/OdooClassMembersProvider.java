import com.google.gson.Gson;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import com.jetbrains.python.codeInsight.PyCustomMember;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyPsiFacade;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.impl.PyPsiFacadeImpl;
import com.jetbrains.python.psi.resolve.PyResolveContext;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyOverridingClassMembersProvider;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class OdooClassMembersProvider implements PyOverridingClassMembersProvider {
    @Override
    public @NotNull Collection<PyCustomMember> getMembers(PyClassType pyClassType, PsiElement psiElement, @NotNull TypeEvalContext typeEvalContext) {
        return Collections.emptyList();
    }

    @Override
    public @Nullable PsiElement resolveMember(@NotNull PyClassType pyClassType, @NotNull String s, @Nullable PsiElement psiElement, @NotNull PyResolveContext pyResolveContext) {
//        PyClass pyClass = pyClassType.getPyClass();
//        String qName = pyClass.getQualifiedName();
//        TypeEvalContext typeEvalContext = pyResolveContext.getTypeEvalContext();
//        Project project = pyClass.getProject();
//
//        PsiFile psiFile = pyClass.getContainingFile();
//        VirtualFile virtualFile = psiFile.getVirtualFile();
//        FileBasedIndex fileIndex = FileBasedIndex.getInstance();
//        GlobalSearchScope scope = GlobalSearchScope.fileScope(psiFile);
//
//        AtomicReference<String> modelRef = new AtomicReference<>();
//        fileIndex.processAllKeys(OdooModelIndex.NAME, key -> {
//            fileIndex.processValues(OdooModelIndex.NAME, key, virtualFile, (file, value) -> {
//                HashMap info = new Gson().fromJson(value, HashMap.class);
//                if (info.get("classQName").equals(qName)) {
//                    modelRef.set(key);
//                    return false;
//                }
//                return true;
//            }, scope);
//            return modelRef.get() == null;
//        }, scope, null);
//
//        String model = modelRef.get();
//        if (model == null) {
//            return null;
//        }
//
//        PsiElement result = findClassMember(pyClass, s, typeEvalContext);
//        if (result != null) {
//            return result;
//        }
//
//        List<String> classQNames = new LinkedList<>();
//        fileIndex.processValues(OdooModelIndex.NAME, model, null, (file, value) -> {
//            if (!file.equals(virtualFile)) {
//                HashMap info = new Gson().fromJson(value, HashMap.class);
//                if (info.containsKey("classQName")) {
//                    boolean primary = (boolean) info.get("primary");
//                    String classQName = (String) info.get("classQName");
//                    if (primary) {
//                        classQNames.add(0, classQName);
//                    } else {
//                        classQNames.add(classQName);
//                    }
//                }
//            }
//            return true;
//        }, GlobalSearchScope.projectScope(project));
//
//        PyPsiFacade pyPsiFacade = PyPsiFacadeImpl.getInstance(project);
//        for (String classQName : classQNames) {
//            PyClass pyClass1 = pyPsiFacade.createClassByQName(classQName, pyClass);
//            if (pyClass1 != null) {
//                result = findClassMember(pyClass1, s, typeEvalContext);
//                if (result != null) {
//                    return result;
//                }
//            }
//        }

        return null;
    }

    private @Nullable PsiElement findClassMember(PyClass pyClass, String name, TypeEvalContext typeEvalContext) {
        PyTargetExpression attribute = pyClass.findClassAttribute(name, false, typeEvalContext);
        if (attribute != null) {
            return attribute;
        }

        return pyClass.findMethodByName(name, false, typeEvalContext);
    }
}
