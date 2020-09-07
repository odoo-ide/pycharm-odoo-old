package dev.ngocta.pycharm.odoo.python.model;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.MultiMap;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.PyUtil;
import com.jetbrains.python.psi.search.PyClassInheritorsSearch;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModelLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result) {
        Set<PyTargetExpression> attributes = new HashSet<>();
        for (PsiElement element : elements) {
            LineMarkerInfo<PsiElement> markerInfo = null;
            if (element instanceof PyClass) {
                markerInfo = getSuperClassLineMarker((PyClass) element);
            } else if (PyUtil.isClassAttribute(element)) {
                markerInfo = getSuperAttributeLineMarker((PyTargetExpression) element);
                attributes.add((PyTargetExpression) element);
            }
            if (markerInfo != null) {
                result.add(markerInfo);
            }
        }
        collectionOverridingAttributeMakers(attributes, result);
    }

    private static LineMarkerInfo<PsiElement> getSuperClassLineMarker(@NotNull PyClass cls) {
        PsiElement identifier = cls.getNameIdentifier();
        if (identifier == null) {
            return null;
        }
        List<PyClass> ancestors = OdooModelUtils.getModelClassAncestors(cls, null);
        if (ancestors.isEmpty()) {
            return null;
        }
        GutterIconNavigationHandler<PsiElement> navigationHandler = (e, elt) -> {
            PsiElementListNavigator.openTargets(e, ancestors.toArray(PyClass.EMPTY_ARRAY),
                    "Super classes", null, new DefaultPsiElementCellRenderer());
        };
        return new LineMarkerInfo<>(
                identifier,
                identifier.getTextRange(),
                AllIcons.Gutter.OverridingMethod,
                e -> "View super classes",
                navigationHandler,
                GutterIconRenderer.Alignment.RIGHT);
    }

    @Nullable
    private LineMarkerInfo<PsiElement> getSuperAttributeLineMarker(@NotNull PyTargetExpression element) {
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
        for (PyClass ancestor : knownAncestors) {
            if (ancestor.findClassAttribute(name, false, null) != null) {
                return null;
            }
        }
        List<PyClass> unknownAncestors = OdooModelUtils.getUnknownModelClassAncestors(containingClass, context);
        for (PyClass ancestor : unknownAncestors) {
            PyTargetExpression attribute = ancestor.findClassAttribute(name, false, null);
            if (attribute == null) {
                continue;
            }
            GutterIconNavigationHandler<PsiElement> navigationHandler = (e, elt) -> {
                List<NavigatablePsiElement> attributes = new LinkedList<>();
                for (PyClass ancestorInner : unknownAncestors) {
                    PyTargetExpression attributeInner = ancestorInner.findClassAttribute(name, false, null);
                    if (attributeInner != null) {
                        attributes.add(attributeInner);
                    }
                }
                PsiElementListNavigator.openTargets(e, attributes.toArray(NavigatablePsiElement.EMPTY_NAVIGATABLE_ELEMENT_ARRAY),
                        "Super attributes", null, new DefaultPsiElementCellRenderer());
            };
            return new LineMarkerInfo<>(
                    identifier,
                    identifier.getTextRange(),
                    AllIcons.Gutter.OverridingMethod,
                    e -> "View super attributes",
                    navigationHandler,
                    GutterIconRenderer.Alignment.RIGHT);
        }
        return null;
    }

    private static void collectionOverridingAttributeMakers(Set<PyTargetExpression> attributes,
                                                            Collection<? super LineMarkerInfo<?>> result) {
        Set<PyClass> classes = new HashSet<>();
        final MultiMap<PyClass, PyTargetExpression> candidates = new MultiMap<>();
        for (PyTargetExpression attribute : attributes) {
            PyClass pyClass = attribute.getContainingClass();
            if (pyClass != null && attribute.getName() != null) {
                classes.add(pyClass);
                candidates.putValue(pyClass, attribute);
            }
        }
        final Set<PyTargetExpression> overridden = new HashSet<>();
        for (final PyClass pyClass : classes) {
            PyClassInheritorsSearch.search(pyClass, true).forEach(inheritor -> {
                for (Iterator<PyTargetExpression> it = candidates.get(pyClass).iterator(); it.hasNext(); ) {
                    PyTargetExpression attribute = it.next();
                    if (attribute.getName() != null && inheritor.findClassAttribute(attribute.getName(), false, null) != null) {
                        overridden.add(attribute);
                        it.remove();
                    }
                }
                return !candidates.isEmpty();
            });
            if (candidates.isEmpty()) break;
        }
        for (PyTargetExpression attribute : overridden) {
            PsiElement identifier = attribute.getNameIdentifier();
            if (identifier == null || attribute.getName() == null) {
                continue;
            }
            GutterIconNavigationHandler<PsiElement> navigationHandler = (e, elt) -> {
                List<NavigatablePsiElement> navElements = new LinkedList<>();
                PyClass cls = attribute.getContainingClass();
                if (cls == null) {
                    return;
                }
                for (PyClass ancestorInner : PyClassInheritorsSearch.search(cls, true)) {
                    PyTargetExpression attributeInner = ancestorInner.findClassAttribute(attribute.getName(), false, null);
                    if (attributeInner != null) {
                        navElements.add(attributeInner);
                    }
                }
                PsiElementListNavigator.openTargets(e, navElements.toArray(NavigatablePsiElement.EMPTY_NAVIGATABLE_ELEMENT_ARRAY),
                        "Overriding attributes", null, new DefaultPsiElementCellRenderer());
            };
            LineMarkerInfo<PsiElement> maker = new LineMarkerInfo<>(
                    identifier,
                    identifier.getTextRange(),
                    AllIcons.Gutter.OverridenMethod,
                    e -> "View overriding attributes",
                    navigationHandler,
                    GutterIconRenderer.Alignment.RIGHT);
            result.add(maker);
        }
    }
}
