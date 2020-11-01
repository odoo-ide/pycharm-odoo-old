package dev.ngocta.pycharm.odoo.python;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.jetbrains.python.pyi.PyiUtil;
import dev.ngocta.pycharm.odoo.OdooUtils;
import dev.ngocta.pycharm.odoo.python.model.OdooModelClass;
import dev.ngocta.pycharm.odoo.python.model.OdooModelUtils;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooSuperMethodCompletionContributor extends CompletionContributor {
    public OdooSuperMethodCompletionContributor() {
        extend(CompletionType.BASIC,
                psiElement().afterLeafSkipping(psiElement().whitespace(), psiElement().withElementType(PyTokenTypes.DEF_KEYWORD)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getOriginalPosition();
                        if (position == null) {
                            position = parameters.getPosition();
                        }
                        PyClass containingClass = PyUtil.getContainingClassOrSelf(position);
                        if (!OdooUtils.isOdooCode(containingClass)) {
                            return;
                        }
                        OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(containingClass);
                        if (modelClass != null) {
                            containingClass = modelClass;
                        }
                        Set<String> seenNames = new HashSet<>();
                        for (PyFunction function : containingClass.getMethods()) {
                            seenNames.add(function.getName());
                        }
                        TypeEvalContext typeEvalContext = TypeEvalContext.codeCompletion(position.getProject(), parameters.getOriginalFile());
                        List<PyClass> ancestors = containingClass.getAncestorClasses(typeEvalContext);
                        ancestors.remove(containingClass);
                        for (PyClass ancestor : ancestors) {
                            if (PyiUtil.isInsideStub(ancestor)) {
                                ancestor = PyiUtil.getOriginalElementOrLeaveAsIs(ancestor, PyClass.class);
                            }
                            for (PyFunction superMethod : ancestor.getMethods()) {
                                String name = superMethod.getName();
                                if (name == null || name.isEmpty()) {
                                    continue;
                                }
                                if (!seenNames.contains(name)) {
                                    String s = name + superMethod.getParameterList().getText() + ":";
                                    LookupElementBuilder element = LookupElementBuilder.create(s);
                                    result.addElement(element);
                                    seenNames.add(name);
                                }
                            }
                        }
                        result.stopHere();
                    }
                });
    }
}