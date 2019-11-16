import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiPolyVariantReference;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.PyTypeProviderBase;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class OdooTypeProvider extends PyTypeProviderBase {
    @Nullable
    @Override
    public Ref<PyType> getParameterType(@NotNull PyNamedParameter param, @NotNull PyFunction function, @NotNull TypeEvalContext context) {
        if (param.isSelf()) {
            PyClass pyClass = PyUtil.getContainingClassOrSelf(param);
            if (pyClass != null) {
                PsiElement parent = param.getParent();
                if (parent instanceof PyParameterList) {
                    PyParameterList parameterList = (PyParameterList) parent;
                    PyFunction func = parameterList.getContainingFunction();
                    if (func != null) {
                        final PyFunction.Modifier modifier = func.getModifier();
                        OdooModelClassType type = new OdooModelClassType(pyClass, modifier == PyFunction.Modifier.CLASSMETHOD);
                        return Ref.create(type);
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    @Override
    public PyType getReferenceExpressionType(@NotNull PyReferenceExpression referenceExpression, @NotNull TypeEvalContext context) {
        PsiPolyVariantReference variantReference = referenceExpression.getReference();
        PsiElement psiElement = variantReference.resolve();
        if (psiElement instanceof PyTargetExpression) {
            PyTargetExpression targetExpression = (PyTargetExpression) psiElement;
            PyExpression pyExpression = targetExpression.findAssignedValue();
            if (pyExpression instanceof PyCallExpression) {
                PyCallExpression callExpression = (PyCallExpression) pyExpression;
                PyExpression callee = callExpression.getCallee();
                if (callee != null) {
                    String calleeName = callee.getName();
                    if (calleeName != null) {
                        if (calleeName.equals("Many2one")
                                || calleeName.equals("One2many")
                                || calleeName.equals("Many2many")) {
                            PyStringLiteralExpression comodelExpression = callExpression.getArgument(
                                    0, "comodel_name", PyStringLiteralExpression.class);
                            if (comodelExpression != null) {
                                String comodel = comodelExpression.getStringValue();
                                PyClass pyClass = findModelClass(comodel, referenceExpression);
                                if (pyClass != null) {
                                    return new OdooModelClassType(pyClass, false);
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @Nullable
    private PyClass findModelClass(@NotNull String model, @NotNull PsiElement anchor) {
        Project project = anchor.getProject();
        PsiFile psiFile = anchor.getContainingFile();
        if (psiFile != null) {
            VirtualFile moduleDir = Utils.getOdooModuleDir(psiFile.getVirtualFile());
            if (moduleDir != null) {
                return findModelClass(model, moduleDir.getName(), project, new LinkedList<>());
            }
        }
        return null;
    }

    @Nullable
    private PyClass findModelClass(@NotNull String model, @NotNull String moduleName, @NotNull Project project, List<String> visitedModuleNames) {
        if (visitedModuleNames.contains(moduleName)) {
            return null;
        }
        visitedModuleNames.add(moduleName);
        List<PyClass> pyClasses = OdooModelIndex.findModelClasses(model, moduleName, project);
        if (!pyClasses.isEmpty()) {
            return pyClasses.get(0);
        }
        List<String> depends = OdooModuleIndex.getDepends(moduleName, project);
        for (String depend : depends) {
            PyClass pyClass = findModelClass(model, depend, project, visitedModuleNames);
            if (pyClass != null) {
                return pyClass;
            }
        }
        return null;
    }

    @Override
    public Ref<PyType> getReferenceType(@NotNull PsiElement referenceTarget, @NotNull TypeEvalContext context, @Nullable PsiElement anchor) {
        if (referenceTarget instanceof PyTargetExpression) {
            PyTargetExpression targetExpression = (PyTargetExpression) referenceTarget;
            PsiElement parent = targetExpression.getParent();
            if (parent instanceof PyForPart) {
                PyForPart forPart = (PyForPart) parent;
                PyExpression source = forPart.getSource();
                if (source instanceof PyReferenceExpression) {
                    PyType referenceType = context.getType(source);
                    if (referenceType instanceof OdooModelClassType) {
                        return Ref.create(referenceType);
                    }
                }
            }
        }
        return null;
    }
}
