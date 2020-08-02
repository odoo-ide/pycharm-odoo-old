package dev.ngocta.pycharm.odoo.data;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.PlatformIcons;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import com.intellij.util.xml.DomTarget;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.data.filter.OdooRecordFilter;
import dev.ngocta.pycharm.odoo.python.module.OdooModule;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomRecordLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class OdooExternalIdReference extends PsiReferenceBase.Poly<PsiElement> {
    private final OdooRecordFilter myFilter;
    private final boolean myAllowUnqualified;

    public OdooExternalIdReference(@NotNull PsiElement element,
                                   @Nullable TextRange rangeInElement,
                                   @Nullable OdooRecordFilter filter,
                                   boolean allowUnqualified) {
        super(element, rangeInElement, false);
        myFilter = filter;
        myAllowUnqualified = allowUnqualified;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return PsiElementResolveResult.createResults(resolveInner());
    }

    @NotNull
    protected List<PsiElement> resolveInner() {
        return PyUtil.getParameterizedCachedValue(getElement(), null, param -> {
            List<OdooRecord> records = OdooExternalIdIndex.findRecordsById(getValue(), getElement(), myAllowUnqualified);
            List<PsiElement> elements = new LinkedList<>();
            records.forEach(record -> elements.addAll(record.getNavigationElements(getElement().getProject())));
            return OdooModuleUtils.sortElementByOdooModuleDependOrder(elements, true);
        });
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(getElement());
        if (module == null) {
            return new Object[0];
        }
        List<LookupElement> elements = new LinkedList<>();
        GlobalSearchScope scope = module.getOdooModuleWithDependenciesScope();
        Project project = getElement().getProject();
        OdooExternalIdIndex.processAllRecords(project, scope, record -> {
            if (myFilter == null || myFilter.accept(record)) {
                List<String> ids = new LinkedList<>();
                ids.add(record.getQualifiedId());
                if (myAllowUnqualified && record.getOriginModule().equals(module.getName())) {
                    ids.add(record.getUnqualifiedId());
                }
                ids.forEach(id -> {
                    LookupElement element = LookupElementBuilder.create(id)
                            .withTypeText(record.getModel())
                            .withIcon(PlatformIcons.XML_TAG_ICON);
                    elements.add(element);
                });
            }
            return true;
        });
        return elements.toArray();
    }

    @Override
    public boolean isReferenceTo(@NotNull PsiElement element) {
        if (isRecordDefinition(element)) {
            OdooModule odooModule = OdooModuleUtils.getContainingOdooModule(getElement());
            OdooModule referenceToOdooModule = OdooModuleUtils.getContainingOdooModule(element);
            if (odooModule != null && referenceToOdooModule != null) {
                if (getValue().startsWith(referenceToOdooModule.getName() + ".")) {
                    return true;
                }
                return odooModule.equals(referenceToOdooModule) && !getValue().contains(".");
            }
        }
        return super.isReferenceTo(element);
    }

    private boolean isRecordDefinition(@NotNull PsiElement element) {
        if (element instanceof XmlTag) {
            DomElement domElement = DomManager.getDomManager(element.getProject()).getDomElement((XmlTag) element);
            return domElement instanceof OdooDomRecordLike;
        } else if (element instanceof PomTargetPsiElement) {
            PomTarget target = ((PomTargetPsiElement) element).getTarget();
            if (target instanceof DomTarget) {
                return ((DomTarget) target).getDomElement() instanceof OdooDomRecordLike;
            }
        }
        return false;
    }

    @Override
    public PsiElement handleElementRename(@NotNull String newElementName) throws IncorrectOperationException {
        if (getValue().contains(".")) {
            newElementName = getValue().split("\\.")[0] + "." + newElementName;
        }
        return super.handleElementRename(newElementName);
    }
}
