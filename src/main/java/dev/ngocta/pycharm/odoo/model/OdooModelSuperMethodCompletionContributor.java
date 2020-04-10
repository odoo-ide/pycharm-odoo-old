package dev.ngocta.pycharm.odoo.model;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.PyNames;
import com.jetbrains.python.PyTokenTypes;
import com.jetbrains.python.psi.LanguageLevel;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.jetbrains.python.pyi.PyiUtil;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class OdooModelSuperMethodCompletionContributor extends CompletionContributor {
    public OdooModelSuperMethodCompletionContributor() {
        extend(CompletionType.BASIC,
                psiElement().afterLeafSkipping(psiElement().whitespace(), psiElement().withElementType(PyTokenTypes.DEF_KEYWORD)),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getPosition();
                        PyClass containingClass = PyUtil.getContainingClassOrSelf(position);
                        if (containingClass == null) {
                            return;
                        }
                        OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(containingClass);
                        if (modelClass == null) {
                            return;
                        }
                        Set<String> seenNames = new HashSet<>();
                        for (PyFunction function : modelClass.getMethods()) {
                            seenNames.add(function.getName());
                        }
                        LanguageLevel languageLevel = LanguageLevel.forElement(parameters.getOriginalFile());
                        seenNames.addAll(PyNames.getBuiltinMethods(languageLevel).keySet());
                        for (PyFunction method : containingClass.getMethods()) {
                            seenNames.add(method.getName());
                        }
                        TypeEvalContext typeEvalContext = TypeEvalContext.codeCompletion(position.getProject(), parameters.getOriginalFile());
                        List<PyClass> ancestors = modelClass.getAncestorClasses(typeEvalContext);
                        ancestors.remove(containingClass);
                        for (PyClass ancestor : ancestors) {
                            if (PyiUtil.isInsideStub(ancestor)) {
                                ancestor = PyiUtil.getOriginalElementOrLeaveAsIs(ancestor, PyClass.class);
                            }
                            for (PyFunction superMethod : ancestor.getMethods()) {
                                if (!seenNames.contains(superMethod.getName())) {
                                    String s = superMethod.getName() + superMethod.getParameterList().getText() + ":";
                                    LookupElementBuilder element = LookupElementBuilder.create(s);
                                    result.addElement(element);
                                    seenNames.add(superMethod.getName());
                                }
                            }
                        }
                        result.stopHere();
                    }
                });
    }
}