package dev.ngocta.pycharm.odoo;

import com.intellij.lang.html.HTMLLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.patterns.PatternCondition;
import com.intellij.patterns.XmlElementPattern;
import com.intellij.patterns.XmlPatterns;
import com.intellij.psi.ElementManipulators;
import com.intellij.psi.InjectedLanguagePlaces;
import com.intellij.psi.LanguageInjector;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlText;
import com.intellij.util.ProcessingContext;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomManager;
import dev.ngocta.pycharm.odoo.xml.dom.OdooDomFieldAssignment;
import org.jetbrains.annotations.NotNull;

public class OdooHtmlLanguageInjector implements LanguageInjector {
    public static final XmlElementPattern.XmlTextPattern MAIL_TEMPLATE_BODY_PATTERN =
            XmlPatterns.xmlText().with(new PatternCondition<XmlText>("mailTemplateBody") {
                @Override
                public boolean accepts(@NotNull XmlText xmlText,
                                       ProcessingContext context) {
                    XmlTag tag = xmlText.getParentTag();
                    if (tag == null) {
                        return false;
                    }
                    Project project = xmlText.getProject();
                    DomManager domManager = DomManager.getDomManager(project);
                    DomElement domElement = domManager.getDomElement(tag);
                    if (domElement instanceof OdooDomFieldAssignment) {
                        OdooDomFieldAssignment fieldAssignment = (OdooDomFieldAssignment) domElement;
                        String field = fieldAssignment.getName().getStringValue();
                        String model = fieldAssignment.getModel();
                        return "body_html".equals(field) && OdooNames.MAIL_TEMPLATE.equals(model);
                    }
                    return false;
                }
            });

    @Override
    public void getLanguagesToInject(@NotNull PsiLanguageInjectionHost host,
                                     @NotNull InjectedLanguagePlaces injectionPlacesRegistrar) {
        if (MAIL_TEMPLATE_BODY_PATTERN.accepts(host)) {
            TextRange range = ElementManipulators.getValueTextRange(host);
            injectionPlacesRegistrar.addPlace(HTMLLanguage.INSTANCE, range, null, null);
        }
    }
}
