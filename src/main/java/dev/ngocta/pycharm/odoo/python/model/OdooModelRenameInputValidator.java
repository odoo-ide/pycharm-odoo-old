package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenameInputValidator;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class OdooModelRenameInputValidator implements RenameInputValidator {
    @Override
    @NotNull
    public ElementPattern<? extends PsiElement> getPattern() {
        return PlatformPatterns.psiElement().with(new PatternCondition<PsiElement>("") {
            @Override
            public boolean accepts(@NotNull PsiElement element, ProcessingContext context) {
                return element instanceof OdooModelClass;
            }
        });
    }

    @Override
    public boolean isInputValid(@NotNull String newName, @NotNull PsiElement element, @NotNull ProcessingContext context) {
        return Pattern.matches("^[a-z0-9_.]+$", newName);
    }
}
