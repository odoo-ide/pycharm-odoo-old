package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.lang.ecmascript6.resolve.ES6TypeEvaluator;
import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSType;
import com.intellij.lang.javascript.psi.resolve.JSEvaluateContext;
import com.intellij.lang.javascript.psi.resolve.JSTypeEvaluationHelper;
import com.intellij.lang.javascript.psi.resolve.JSTypeProcessor;
import com.intellij.lang.javascript.psi.types.evaluable.JSRequireCallExpressionType;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OdooJSTypeEvaluator extends ES6TypeEvaluator {
    public OdooJSTypeEvaluator(@NotNull JSEvaluateContext context,
                               @NotNull JSTypeProcessor processor,
                               @NotNull JSTypeEvaluationHelper helper) {
        super(context, processor, helper);
    }

    @Override
    protected void evaluateCallExpressionTypes(@NotNull JSCallExpression callExpression) {
        if (callExpression.isRequireCall() && OdooJSUtils.isInOdooJSModule(callExpression)) {
            JSExpression arg = callExpression.getArguments()[0];
            if (arg instanceof JSLiteralExpression) {
                String moduleName = ((JSLiteralExpression) arg).getStringValue();
                if (moduleName != null) {
                    JSType type = getModuleReturnType(moduleName, callExpression);
                    addType(type, callExpression);
                    return;
                }
            }
        }
        super.evaluateCallExpressionTypes(callExpression);
    }

    @Nullable
    private JSType getModuleReturnType(@NotNull String moduleName,
                                       @NotNull PsiElement anchor) {
        OdooJSModule module = OdooJSModuleIndex.findModule(moduleName, anchor);
        return module != null ? module.getReturnType() : null;
    }

    @Override
    protected void doAddType(@Nullable JSType type,
                             @Nullable PsiElement source,
                             boolean skipGuard) {
        if (type instanceof JSRequireCallExpressionType) {
            String moduleName = ((JSRequireCallExpressionType) type).resolveReferencedModule();
            PsiElement sourceElement = type.getSourceElement();
            if (sourceElement != null && OdooJSUtils.isInOdooJSModule(sourceElement)) {
                JSType moduleType = getModuleReturnType(moduleName, sourceElement);
                if (moduleType != null) {
                    super.doAddType(moduleType, source, skipGuard);
                    return;
                }
            }
        }
        super.doAddType(type, source, skipGuard);
    }

    @Override
    public void addTypeFromExternalModuleReferenceResolveResult(@NotNull PsiElement resolve,
                                                                boolean fromRequire) {
        if (resolve instanceof OdooJSModule) {
            JSType type = ((OdooJSModule) resolve).getReturnType();
            if (type != null) {
                addType(type, resolve);
            }
            return;
        }
        super.addTypeFromExternalModuleReferenceResolveResult(resolve, fromRequire);
    }
}
