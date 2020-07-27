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
import com.intellij.pom.Navigatable;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlTokenType;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import dev.ngocta.pycharm.odoo.data.OdooExternalIdIndex;
import dev.ngocta.pycharm.odoo.data.OdooRecord;
import dev.ngocta.pycharm.odoo.data.OdooViewInheritIdIndex;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRecordLike;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomViewInheritLocator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class OdooXmlLineMarkerProvider implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo<PsiElement> getLineMarkerInfo(@NotNull PsiElement element) {
        ASTNode node = element.getNode();
        if (node != null && node.getElementType() == XmlTokenType.XML_START_TAG_START && element.getParent() instanceof XmlTag) {
            Project project = element.getProject();
            DomElement domElement = DomManager.getDomManager(project).getDomElement((XmlTag) element.getParent());
            if (domElement instanceof OdooDomViewInheritLocator) {
                return getInheritedElementsLineMarker(element, (OdooDomViewInheritLocator) domElement);
            } else if (domElement instanceof OdooDomRecordLike) {
                return getOverridingViews(element, (OdooDomRecordLike) domElement);
            }
        }
        return null;
    }

    @Nullable
    private LineMarkerInfo<PsiElement> getInheritedElementsLineMarker(@NotNull PsiElement identifier,
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
    private LineMarkerInfo<PsiElement> getOverridingViews(@NotNull PsiElement identifier,
                                                          @NotNull OdooDomRecordLike domRecord) {
        OdooRecord record = domRecord.getRecord();
        if (record == null) {
            return null;
        }
        OdooModule odooModule = domRecord.getOdooModule();
        if (odooModule == null) {
            return null;
        }
        Project project = identifier.getProject();
        GlobalSearchScope scope = odooModule.getOdooModuleWithExtensionsScope();
        boolean hasOverriding = OdooViewInheritIdIndex.hasOverridingId(record.getQualifiedId(), scope);
        if (!hasOverriding) {
            return null;
        }
        GutterIconNavigationHandler<PsiElement> navigationHandler = (e, elt) -> {
            List<String> overridingIds = OdooViewInheritIdIndex.getOverridingIds(record.getQualifiedId(), scope);
            List<OdooRecord> records = new LinkedList<>();
            OdooExternalIdIndex.processRecordsByIds(project, scope, r -> {
                records.add(r);
                return true;
            }, overridingIds);
            List<NavigatablePsiElement> elements = new LinkedList<>();
            for (OdooRecord r : records) {
                elements.addAll(r.getNavigationElements(project));
            }
            elements = OdooModuleUtils.sortElementByOdooModuleDependOrder(elements, true);
            PsiElementListNavigator.openTargets(
                    e, elements.toArray(NavigatablePsiElement.EMPTY_NAVIGATABLE_ELEMENT_ARRAY),
                    "Overriding views", null, new DefaultPsiElementCellRenderer());
        };
        return new LineMarkerInfo<>(
                identifier,
                identifier.getTextRange(),
                AllIcons.Gutter.OverridenMethod,
                e -> "View overriding views",
                navigationHandler,
                GutterIconRenderer.Alignment.RIGHT);
    }
}
