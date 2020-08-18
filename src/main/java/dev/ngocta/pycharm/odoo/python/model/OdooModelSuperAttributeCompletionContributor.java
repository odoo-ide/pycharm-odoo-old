package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyExpressionStatement;
import com.jetbrains.python.psi.PyReferenceExpression;
import com.jetbrains.python.psi.PyStatementList;
import com.jetbrains.python.psi.types.TypeEvalContext;
import gnu.trove.THashSet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class OdooModelSuperAttributeCompletionContributor extends CompletionContributor {
    public OdooModelSuperAttributeCompletionContributor() {
        extend(CompletionType.BASIC,
                PlatformPatterns.psiElement()
                        .withParents(PyReferenceExpression.class, PyExpressionStatement.class, PyStatementList.class, PyClass.class),
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    protected void addCompletions(@NotNull CompletionParameters parameters,
                                                  @NotNull ProcessingContext context,
                                                  @NotNull CompletionResultSet result) {
                        PsiElement position = parameters.getOriginalPosition();
                        if (position == null) {
                            position = parameters.getPosition();
                        }
                        PyClass containingClass = PsiTreeUtil.getParentOfType(position, PyClass.class);
                        if (containingClass == null) {
                            return;
                        }
                        TypeEvalContext typeEvalContext = TypeEvalContext.codeCompletion(containingClass.getProject(), containingClass.getContainingFile());
                        List<PyClass> unknownAncestors = OdooModelUtils.getUnknownModelClassAncestors(containingClass, typeEvalContext);
                        Set<String> seenNames = new THashSet<>();
                        for (PyClass ancestor : unknownAncestors) {
                            ancestor.visitClassAttributes(pyTargetExpression -> {
                                LookupElement lookupElement = OdooModelUtils.createLookupElement(pyTargetExpression, typeEvalContext);
                                if (lookupElement != null && !seenNames.contains(lookupElement.getLookupString())) {
                                    seenNames.add(lookupElement.getLookupString());
                                    result.consume(lookupElement);
                                }
                                return true;
                            }, false, null);
                        }
                    }
                }
        );
    }
}
