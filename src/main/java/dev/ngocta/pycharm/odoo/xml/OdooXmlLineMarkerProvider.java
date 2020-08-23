package dev.ngocta.pycharm.odoo.xml;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.daemon.impl.PsiElementListNavigator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.pom.Navigatable;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomJSTemplate;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRecordLike;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomViewInheritLocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class OdooXmlLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        return null;
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<? extends PsiElement> elements,
                                       @NotNull Collection<? super LineMarkerInfo<?>> result) {
        for (PsiElement element : elements) {
            ASTNode node = element.getNode();
            if (node != null && node.getElementType() == XmlTokenType.XML_START_TAG_START && element.getParent() instanceof XmlTag) {
                Project project = element.getProject();
                DomElement domElement = DomManager.getDomManager(project).getDomElement((XmlTag) element.getParent());
                LineMarkerInfo<PsiElement> markerInfo = null;
                if (domElement instanceof OdooDomViewInheritLocator) {
                    markerInfo = getInheritedElementLineMarker(element, (OdooDomViewInheritLocator) domElement);
                } else if (domElement instanceof OdooDomRecordLike) {
                    markerInfo = getChildrenViewRecordLineMarker(element, (OdooDomRecordLike) domElement);
                } else if (domElement instanceof OdooDomJSTemplate) {
                    markerInfo = getChildrenJSTemplateLineMarker(element, (OdooDomJSTemplate) domElement);
                }
                if (markerInfo != null) {
                    result.add(markerInfo);
                }
            }
        }
    }

    @Nullable
    private LineMarkerInfo<PsiElement> getInheritedElementLineMarker(@NotNull PsiElement identifier,
                                                                     @NotNull OdooDomViewInheritLocator locator) {
        if (locator.getInheritedElement() == null) {
            return null;
        }
        return new LineMarkerInfo<>(
                identifier,
                identifier.getTextRange(),
                AllIcons.Gutter.OverridingMethod,
                e -> "View inherited element",
                (e, elt) -> {
                    XmlTag inheritedElement = locator.getInheritedElement();
                    if (inheritedElement instanceof Navigatable) {
                        ((Navigatable) inheritedElement).navigate(true);
                    }
                },
                GutterIconRenderer.Alignment.LEFT);
    }

    @Nullable
    private LineMarkerInfo<PsiElement> getChildrenViewRecordLineMarker(@NotNull PsiElement identifier,
                                                                       @NotNull OdooDomRecordLike domRecord) {
        OdooRecord record = domRecord.getRecord();
        if (record == null) {
            return null;
        }
        OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(identifier);
        if (odooModule == null) {
            return null;
        }
        Project project = identifier.getProject();
        GlobalSearchScope scope = odooModule.getOdooModuleWithExtensionsScope();
        Ref<Boolean> hasChildrenViews = Ref.create(false);
        OdooViewInheritIdIndex.processChildrenViewRecords(record.getQualifiedId(), dr -> {
            hasChildrenViews.set(true);
            return false;
        }, scope, project);
        if (!hasChildrenViews.get()) {
            return null;
        }

        GutterIconNavigationHandler<PsiElement> navigationHandler = (e, elt) -> {
            List<OdooRecord> records = new LinkedList<>();
            OdooViewInheritIdIndex.processChildrenViewRecords(record.getQualifiedId(), dr -> {
                OdooRecord r = dr.getRecord();
                if (r != null) {
                    records.add(r);
                }
                return true;
            }, scope, project);
            List<NavigatablePsiElement> elements = new LinkedList<>();
            for (OdooRecord r : records) {
                elements.addAll(r.getNavigationElements(project));
            }
            elements = OdooModuleUtils.sortElementByOdooModuleDependOrder(elements, true);
            PsiElementListNavigator.openTargets(
                    e, elements.toArray(NavigatablePsiElement.EMPTY_NAVIGATABLE_ELEMENT_ARRAY),
                    "Children views", null, new DefaultPsiElementCellRenderer());
        };
        return new LineMarkerInfo<>(
                identifier,
                identifier.getTextRange(),
                AllIcons.Gutter.OverridenMethod,
                e -> "View children views",
                navigationHandler,
                GutterIconRenderer.Alignment.RIGHT);
    }

    @Nullable
    private LineMarkerInfo<PsiElement> getChildrenJSTemplateLineMarker(@NotNull PsiElement identifier,
                                                                       @NotNull OdooDomJSTemplate template) {
        OdooModule odooModule = template.getOdooModule();
        if (odooModule == null) {
            return null;
        }
        Project project = identifier.getProject();
        GlobalSearchScope scope = odooModule.getOdooModuleWithExtensionsScope();
        Ref<Boolean> hasChildrenTemplates = Ref.create(false);
        OdooViewInheritIdIndex.processChildrenJSTemplates(template, t -> {
            hasChildrenTemplates.set(true);
            return false;
        }, scope, project);
        if (!hasChildrenTemplates.get()) {
            return null;
        }

        GutterIconNavigationHandler<PsiElement> navigationHandler = (e, elt) -> {
            List<OdooJSTemplateElement> elements = new LinkedList<>();
            OdooViewInheritIdIndex.processChildrenJSTemplates(template, t -> {
                OdooJSTemplateElement element = t.getNavigationElement();
                if (element != null) {
                    elements.add(element);
                }
                return true;
            }, scope, project);
            PsiElementListNavigator.openTargets(
                    e, elements.toArray(NavigatablePsiElement.EMPTY_NAVIGATABLE_ELEMENT_ARRAY),
                    "Children templates", null, new DefaultPsiElementCellRenderer());
        };
        return new LineMarkerInfo<>(
                identifier,
                identifier.getTextRange(),
                AllIcons.Gutter.OverridenMethod,
                e -> "View children templates",
                navigationHandler,
                GutterIconRenderer.Alignment.RIGHT);
    }
}
