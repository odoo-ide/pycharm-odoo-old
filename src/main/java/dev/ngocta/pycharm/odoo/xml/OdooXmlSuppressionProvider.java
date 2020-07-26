package dev.ngocta.pycharm.odoo.xml;

import com.intellij.codeInspection.XmlSuppressionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class OdooXmlSuppressionProvider extends XmlSuppressionProvider {
    @Override
    public boolean isProviderAvailable(@NotNull PsiFile file) {
        return OdooXmlUtils.isOdooXmlDataFile(file);
    }

    @Override
    public boolean isSuppressedFor(@NotNull PsiElement element,
                                   @NotNull String inspectionId) {
        return inspectionId.equals("CheckTagEmptyBody") || inspectionId.equals("CheckValidXmlInScriptTagBody");
    }

    @Override
    public void suppressForFile(@NotNull PsiElement element,
                                @NotNull String inspectionId) {

    }

    @Override
    public void suppressForTag(@NotNull PsiElement element,
                               @NotNull String inspectionId) {

    }
}
