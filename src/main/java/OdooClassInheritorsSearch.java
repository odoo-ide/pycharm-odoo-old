import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.ProjectScope;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.search.PyClassInheritorsSearch;
import com.jetbrains.python.psi.stubs.PySuperClassIndex;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OdooClassInheritorsSearch implements QueryExecutor<PyClass, PyClassInheritorsSearch.SearchParameters> {
    @Override
    public boolean execute(PyClassInheritorsSearch.@NotNull SearchParameters queryParameters, @NotNull Processor<? super PyClass> consumer) {
        Set<PyClass> processed = new HashSet<>();
        return processDirectInheritors(queryParameters.getSuperClass(), consumer, queryParameters.isCheckDeepInheritance(), processed);
    }
    private static boolean processDirectInheritors(
            final PyClass superClass, final Processor<? super PyClass> consumer, final boolean checkDeep, final Set<PyClass> processed
    ) {
        return ReadAction.compute(() -> {
            final String superClassName = superClass.getName();
            if (processed.contains(superClass)) return true;
            processed.add(superClass);
            Project project = superClass.getProject();
            final Collection<PyClass> candidates = StubIndex.getElements(PySuperClassIndex.KEY, superClassName, project,
                    ProjectScope.getAllScope(project), PyClass.class);
            for (PyClass candidate : candidates) {
                final PyClass[] classes = candidate.getSuperClasses(null);
                for (PyClass superClassCandidate : classes) {
                    if (superClassCandidate.isEquivalentTo(superClass)) {
                        if (!consumer.process(candidate)) {
                            return false;
                        }
                        if (checkDeep && !processDirectInheritors(candidate, consumer, checkDeep, processed)) return false;
                        break;
                    }
                }
            }
            return true;
        });
    }
}
