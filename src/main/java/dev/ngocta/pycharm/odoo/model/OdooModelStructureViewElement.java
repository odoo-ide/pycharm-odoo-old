package dev.ngocta.pycharm.odoo.model;

import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.navigation.ColoredItemPresentation;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.util.PlatformIcons;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyElementVisitor;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.impl.PyPsiUtils;
import com.jetbrains.python.psi.types.TypeEvalContext;
import com.jetbrains.python.structureView.PyStructureViewElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public class OdooModelStructureViewElement extends PyStructureViewElement {
    public OdooModelStructureViewElement(PyElement element) {
        super(element);
    }

    protected OdooModelStructureViewElement(PyElement element, Visibility visibility, boolean inherited, boolean field) {
        super(element, visibility, inherited, field);
    }

    @NotNull
    @Override
    public StructureViewTreeElement[] getChildren() {
        final PyElement element = getValue();
        if (element == null) {
            return EMPTY_ARRAY;
        }

        final Collection<StructureViewTreeElement> children = new ArrayList<>();
        for (PyElement e : getElementChildren(element)) {
            children.add(new OdooModelStructureViewElement(e, Visibility.NORMAL, false, elementIsField(e)));
        }
        PyPsiUtils.assertValid(element);
        if (element instanceof PyClass) {
            OdooModelClass modelClass = OdooModelUtils.getContainingOdooModelClass(element);
            if (modelClass != null) {
                final TypeEvalContext context = TypeEvalContext.codeAnalysis(element.getProject(), element.getContainingFile());
                List<PyClass> ancestors = modelClass.getAncestorClasses(context);
                for (PyClass cls : ancestors) {
                    if (cls.equals(element)) {
                        continue;
                    }
                    for (PyElement e : getElementChildren(cls)) {
                        final StructureViewTreeElement inherited = new OdooModelStructureViewElement(e, Visibility.NORMAL, true, elementIsField(e));
                        if (!children.contains(inherited)) {
                            children.add(inherited);
                        }
                    }
                }
            }
        }
        return children.toArray(StructureViewTreeElement.EMPTY_ARRAY);
    }

    private Collection<PyElement> getElementChildren(final PyElement element) {
        Collection<PyElement> children = new LinkedHashSet<>();
        PyPsiUtils.assertValid(element);
        element.acceptChildren(new PyElementVisitor() {
            @Override
            public void visitElement(PsiElement e) {
                if (isWorthyItem(e, element)) {
                    children.add((PyElement) e);
                } else {
                    e.acceptChildren(this);
                }
            }
        });
        return children;
    }

    @Override
    protected boolean isWorthyItem(@Nullable PsiElement element, @Nullable PsiElement parent) {
        if (element instanceof PyTargetExpression && parent instanceof PyClass) {
            return true;
        }
        return super.isWorthyItem(element, parent);
    }

    @NotNull
    @Override
    public ItemPresentation getPresentation() {
        PyElement element = getValue();
        if (element instanceof PyTargetExpression) {
            OdooFieldInfo info = OdooFieldInfo.getInfo((PyTargetExpression) element);
            if (info != null) {
                return new ColoredItemPresentation() {
                    @Nullable
                    @Override
                    public TextAttributesKey getTextAttributesKey() {
                        if (isInherited()) {
                            return CodeInsightColors.NOT_USED_ELEMENT_ATTRIBUTES;
                        }
                        return null;
                    }

                    @Nullable
                    @Override
                    public String getPresentableText() {
                        String text = element.getName();
                        if (text != null) {
                            text += " | " + info.getTypeName();
                            if (info.getComodel() != null) {
                                text += " (" + info.getComodel() + ")";
                            }
                            return text;
                        }
                        return null;
                    }

                    @Nullable
                    @Override
                    public String getLocationString() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public Icon getIcon(boolean unused) {
                        return PlatformIcons.FIELD_ICON;
                    }
                };
            }
        }
        return super.getPresentation();
    }
}
