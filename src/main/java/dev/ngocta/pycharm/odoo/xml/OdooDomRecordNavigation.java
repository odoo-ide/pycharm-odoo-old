package dev.ngocta.pycharm.odoo.xml;

import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class OdooDomRecordNavigation implements NavigationItem {
    private final OdooDomRecord myRecord;
    private final XmlTag myTag;

    public OdooDomRecordNavigation(OdooDomRecord record) {
        myRecord = record;
        myTag = record.getXmlTag();
    }

    @Nullable
    @Override
    public String getName() {
        return myRecord.getQualifiedId();
    }

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return new ItemPresentation() {
            @NotNull
            @Override
            public String getPresentableText() {
                return getName() + " (" + myRecord.getModel().getValue() + ")";
            }

            @Nullable
            @Override
            public String getLocationString() {
                PsiFile file = myTag.getContainingFile();
                if (file != null) {
                    return file.getName();
                }
                return null;
            }

            @Nullable
            @Override
            public Icon getIcon(boolean unused) {
                return PlatformIcons.XML_TAG_ICON;
            }
        };
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (myTag instanceof NavigationItem) {
            ((NavigationItem) myTag).navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        if (myTag instanceof NavigationItem) {
            return ((NavigationItem) myTag).canNavigate();
        }
        return false;
    }

    @Override
    public boolean canNavigateToSource() {
        if (myTag instanceof NavigationItem) {
            return ((NavigationItem) myTag).canNavigateToSource();
        }
        return false;
    }
}
