package dev.ngocta.pycharm.odoo.model;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class OdooModelSuperAttributeLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PyTargetExpression && PyUtil.isClassAttribute(element)) {
            return getAttributeMarker((PyTargetExpression) element);
        }
        return null;
    }

    private LineMarkerInfo<PsiElement> getAttributeMarker(PyTargetExpression element) {
        String name = element.getName();
        if (name == null) {
            return null;
        }
        PsiElement identifier = element.getNameIdentifier();
        if (identifier == null) {
            return null;
        }
        PyClass containingClass = element.getContainingClass();
        if (containingClass == null) {
            return null;
        }
        OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(containingClass);
        if (modelClass == null) {
            return null;
        }
        TypeEvalContext context = TypeEvalContext.codeAnalysis(element.getProject(), element.getContainingFile());
        List<PyClass> knownAncestors = containingClass.getAncestorClasses(context);
        for (PyClass ancestorClass : knownAncestors) {
            if (ancestorClass.findClassAttribute(name, false, null) != null) {
                return null;
            }
        }
        List<PyClass> unknownAncestors = OdooModelUtils.getUnknownAncestorModelClasses(containingClass, context);
        for (PyClass ancestor : unknownAncestors) {
            PyTargetExpression attribute = ancestor.findClassAttribute(name, false, null);
            if (attribute != null) {
                return new LineMarkerInfo<>(
                        identifier,
                        identifier.getTextRange(),
                        AllIcons.Gutter.OverridingMethod,
                        null,
                        (e, elt) -> {
                            List<NavigatablePsiElement> attributes = new LinkedList<>();
                            for (PyClass ancestorInner : unknownAncestors) {
                                PyTargetExpression attributeInner = ancestorInner.findClassAttribute(name, false, null);
                                if (attributeInner != null) {
                                    attributes.add(attributeInner);
                                }
                            }
                            PsiElementListNavigator.openTargets(e, attributes.toArray(NavigatablePsiElement.EMPTY_NAVIGATABLE_ELEMENT_ARRAY),
                                    "Super attributes", null, new DefaultPsiElementCellRenderer());
                        },
                        GutterIconRenderer.Alignment.RIGHT);
            }
        }
        return null;
    }
}
