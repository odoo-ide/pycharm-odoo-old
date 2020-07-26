package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.QueryExecutor;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.search.PyClassInheritorsSearch;
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
            OdooModule module = OdooModuleUtils.getContainingOdooModule(superClass);
            OdooModelInfo modelInfo = OdooModelInfo.getInfo(superClass);
            if (module != null && modelInfo != null) {
                Project project = superClass.getProject();
                GlobalSearchScope scope = module.getOdooModuleWithExtensionsScope();
                List<String> visitedModels = new LinkedList<>();
                List<String> toVisitModels = new LinkedList<>();
                toVisitModels.add(modelInfo.getName());
                while (!toVisitModels.isEmpty()) {
                    String name = toVisitModels.remove(0);
                    List<PyClass> classes = OdooModelIndex.getOdooModelClassesByName(name, project, scope);
                    classes.remove(superClass);
                    List<PyClass> rightClasses = new LinkedList<>();
                    for (PyClass cls : classes) {
                        OdooModule m = OdooModuleUtils.getContainingOdooModule(cls);
                        if (m != null && (m.equals(module) || m.isDependOn(module))) {
                            rightClasses.add(cls);
                        }
                    }
                    rightClasses = OdooModuleUtils.sortElementByOdooModuleDependOrder(rightClasses, true);
                    for (PyClass rightClass : rightClasses) {
                        if (!consumer.process(rightClass)) {
                            return false;
                        }
                    }
                    visitedModels.add(name);
                    List<PyClass> inheritModelClasses = OdooModelInheritIndex.getOdooModelClassesByInheritModel(name, project, scope);
                    for (PyClass cls : inheritModelClasses) {
                        OdooModule m = OdooModuleUtils.getContainingOdooModule(cls);
                        if (m != null && (m.equals(module) || m.isDependOn(module))) {
                            OdooModelInfo info = OdooModelInfo.getInfo(cls);
                            if (info != null && !visitedModels.contains(info.getName())) {
                                toVisitModels.add(info.getName());
                            }
                        }
                    }
                }
                return false;
            }
            return true;
        });
    }
}
