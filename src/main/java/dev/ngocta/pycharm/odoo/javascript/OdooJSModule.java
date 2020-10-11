package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooJSModule extends JSImplicitElementImpl {
    private final String myName;
    private final JSCallExpression myDefineCallExpression;

    public OdooJSModule(@NotNull String name,
                        @NotNull JSCallExpression defineCallExpression) {
        super(name, defineCallExpression);
        myName = name;
        myDefineCallExpression = defineCallExpression;
    }

    @Override
    @NotNull
    public String getName() {
        return myName;
    }

    @NotNull
    public JSCallExpression getDefineCall() {
        return myDefineCallExpression;
    }

    @Nullable
    public JSLiteralExpression getNameLiteralExpression() {
        JSExpression[] args = getDefineCall().getArguments();
        if (args.length > 0 && args[0] instanceof JSLiteralExpression) {
            return (JSLiteralExpression) args[0];
        }
        return null;
    }

    @Nullable
    public JSFunction getDefinitionFunc() {
        JSExpression[] args = getDefineCall().getArguments();
        if (args.length == 2 && args[1] instanceof JSFunction) {
            return (JSFunction) args[1];
        } else if (args.length == 3 && args[2] instanceof JSFunction) {
            return (JSFunction) args[2];
        }
        return null;
    }

    @Nullable
    public JSType getReturnType() {
        JSFunction function = getDefinitionFunc();
        if (function != null) {
            return function.getReturnType();
        }
        return null;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        JSLiteralExpression literalExpression = getNameLiteralExpression();
        if (literalExpression != null) {
            JSLiteralExpression newLiteralExpression = ElementManipulators.handleContentChange(literalExpression, name);
            JSCallExpression newCallExpression = PsiTreeUtil.getParentOfType(newLiteralExpression, JSCallExpression.class);
            if (newCallExpression != null) {
                return new OdooJSModule(getName(), newCallExpression);
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return "OdooJSModule{" +
                "myName='" + myName + '\'' +
                '}';
    }
}
