package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiTarget;
import com.intellij.psi.impl.PomTargetPsiElementImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.psi.PyClass;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class OdooRecordElement extends PomTargetPsiElementImpl implements NavigatablePsiElement {
    private final OdooRecord myRecord;

    public OdooRecordElement(@NotNull OdooRecord record,
                             @NotNull PsiTarget target) {
        super(target);
        myRecord = record;
    }

    @NotNull
    public OdooRecord getRecord() {
        return myRecord;
    }

    @Override
    public String getName() {
        return myRecord.getUnqualifiedId();
    }

    public String getPresentableText() {
        if (getNavigationElement() instanceof PyClass) {
            ItemPresentation presentation = ((PyClass) getNavigationElement()).getPresentation();
            if (presentation != null) {
                return presentation.getPresentableText();
            }
        }
        String text = myRecord.getQualifiedId();
        if (StringUtil.isNotEmpty(myRecord.getModel())) {
            text += " (" + myRecord.getModel() + ")";
        }
        return text;
    }

    @Override
    public String getLocationString() {
        return OdooModuleUtils.getLocationStringForFile(myRecord.getDataFile());
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        if (getNavigationElement() instanceof XmlTag) {
            XmlAttribute attribute = ((XmlTag) getNavigationElement()).getAttribute("id");
            if (attribute != null) {
                attribute.setValue(name);
                return attribute;
            }
        }
        return super.setName(name);
    }

    @Override
    public boolean isEquivalentTo(PsiElement another) {
        if (another instanceof OdooRecordElement &&
                myRecord.getQualifiedId().equals(((OdooRecordElement) another).myRecord.getQualifiedId())) {
            return true;
        }
        return super.isEquivalentTo(another);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooRecordElement that = (OdooRecordElement) o;
        return myRecord.equals(that.myRecord) &&
                getTarget().equals(that.getTarget());
    }

    @Override
    public int hashCode() {
        return Objects.hash(myRecord, getTarget());
    }
}
