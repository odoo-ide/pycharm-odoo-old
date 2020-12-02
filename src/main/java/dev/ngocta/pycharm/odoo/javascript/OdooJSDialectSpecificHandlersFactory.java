package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.javascript.JavaScriptSpecificHandlersFactory;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluator;
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor;
import org.jetbrains.annotations.NotNull;

public class OdooJSDialectSpecificHandlersFactory extends JavaScriptSpecificHandlersFactory {
    public OdooJSDialectSpecificHandlersFactory() {
    }

    @NotNull
    @Override
    public JSTypeEvaluator newTypeEvaluator(@NotNull JSEvaluateContext context,
                                            @NotNull JSTypeProcessor processor) {
        return new OdooJSTypeEvaluator(context, processor);
    }
}
