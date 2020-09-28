package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.pom.Navigatable;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.FakePsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.resolve.QualifiedNameFinder;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Objects;

public class OdooRecordElement extends FakePsiElement implements NavigatablePsiElement {
    private final OdooRecord myRecord;
    private final PsiElement myElement;

    public OdooRecordElement(@NotNull OdooRecord record,
                             @NotNull PsiElement element) {
        myRecord = record;
        myElement = element;
    }

    @NotNull
    public OdooRecord getRecord() {
        return myRecord;
    }

    @Override
    public String getName() {
        if (myElement instanceof PyElement) {
            return QualifiedNameFinder.getQualifiedName((PyElement) myElement);
        }
        return myRecord.getQualifiedId();
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (myElement instanceof Navigatable) {
            ((Navigatable) myElement).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public PsiElement getParent() {
        return myElement.getParent();
    }

    @Override
    @Nullable
    public TextRange getTextRange() {
        return myElement.getTextRange();
    }

    public String getPresentableText() {
        if (myElement instanceof PyClass) {
            ItemPresentation presentation = ((PyClass) myElement).getPresentation();
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
    public Icon getIcon(boolean unused) {
        return myElement.getIcon(ICON_FLAG_READ_STATUS);
    }

    @Override
    public PsiElement getOriginalElement() {
        return myElement;
    }

    @Override
    public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
        if (myElement instanceof XmlTag) {
            XmlAttribute attribute = ((XmlTag) myElement).getAttribute("id");
            if (attribute != null) {
                attribute.setValue(name);
                return attribute;
            }
        }
        return super.setName(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooRecordElement that = (OdooRecordElement) o;
        return myRecord.getQualifiedId().equals(that.myRecord.getQualifiedId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(myRecord.getQualifiedId());
    }
}
