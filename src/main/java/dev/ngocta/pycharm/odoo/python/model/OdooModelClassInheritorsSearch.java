package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.search.PyClassInheritorsSearch;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

public class OdooModelClassInheritorsSearch implements QueryExecutor<PyClass, PyClassInheritorsSearch.SearchParameters> {
    @Override
    public boolean execute(PyClassInheritorsSearch.@NotNull SearchParameters queryParameters,
                           @NotNull Processor<? super PyClass> consumer) {
        return ReadAction.compute(() -> {
            PyClass superClass = queryParameters.getSuperClass();
            Project project = superClass.getProject();
            OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(superClass);
            if (modelClass == null) {
                return true;
            }
            List<PyClass> ancestors = modelClass.getAncestorClasses(TypeEvalContext.codeAnalysis(project, superClass.getContainingFile()));
            if (ancestors.contains(superClass)) {
                ancestors = ancestors.subList(ancestors.indexOf(superClass), ancestors.size());
            }
            OdooModule module = OdooModuleUtils.getContainingOdooModule(superClass);
            if (module == null) {
                return true;
            }
            GlobalSearchScope scope = module.getOdooModuleWithExtensionsScope();
            List<String> visitedModels = new LinkedList<>();
            List<String> toVisitModels = new LinkedList<>();
            toVisitModels.add(modelClass.getName());
            while (!toVisitModels.isEmpty()) {
                String name = toVisitModels.remove(0);
                List<PyClass> classes = OdooModelIndex.getOdooModelClassesByName(name, project, scope);
                classes.removeAll(ancestors);
                classes = OdooModuleUtils.sortElementByOdooModuleDependOrder(classes, true);
                for (PyClass cls : classes) {
                    if (!consumer.process(cls)) {
                        return false;
                    }
                }
                visitedModels.add(name);
                List<PyClass> inheritModelClasses = OdooModelInheritIndex.getOdooModelClassesByInheritModel(name, project, scope);
                for (PyClass cls : inheritModelClasses) {
                    OdooModelInfo info = OdooModelInfo.getInfo(cls);
                    if (info != null && !visitedModels.contains(info.getName())) {
                        toVisitModels.add(info.getName());
                    }
                }
            }
            return false;
        });
    }
}
