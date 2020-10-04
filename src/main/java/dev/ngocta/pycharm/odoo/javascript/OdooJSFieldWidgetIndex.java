package dev.ngocta.pycharm.odoo.javascript;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.indexing.*;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import dev.ngocta.pycharm.odoo.python.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OdooJSFieldWidgetIndex extends ScalarIndexExtension<String> {
    public static final ID<String, Void> NAME = ID.create("odoo.js.field.widget");
    private static final Pattern REGISTRY_VARIABLE_PATTERN = Pattern.compile("(\\w+)\\s*=\\s*require\\s*\\(\\s*['\"]web\\.field_registry['\"]\\s*\\)");
    private static final String REGISTRY_ADD_REGEX = "(?:\\s*\\.add\\s*\\(\\s*['\"]([a-zA-Z_0-9.]+)['\"]\\s*,\\s*[a-zA-Z_0-9.]+\\)(?:\\s*(?://|/\\*).*)?)";
    private static final Pattern REGISTRY_ADD_PATTERN = Pattern.compile(REGISTRY_ADD_REGEX);

    @Override
    @NotNull
    public ID<String, Void> getName() {
        return NAME;
    }

    @Override
    @NotNull
    public DataIndexer<String, Void, FileContent> getIndexer() {
        return inputData -> {
            Map<String, Void> result = new HashMap<>();
            VirtualFile virtualFile = inputData.getFile();
            if (!OdooModuleUtils.isInOdooModule(virtualFile)) {
                return result;
            }
            String content = inputData.getContentAsText().toString();
            if (!content.contains("web.field_registry")) {
                return result;
            }
            Matcher registryVariableMatcher = REGISTRY_VARIABLE_PATTERN.matcher(content);
            if (registryVariableMatcher.find()) {
                String registryVariableName = registryVariableMatcher.group(1);
                Pattern chainingAddPattern = Pattern.compile(registryVariableName + REGISTRY_ADD_REGEX + "+");
                Matcher chainingAddMatcher = chainingAddPattern.matcher(content);
                while (chainingAddMatcher.find()) {
                    Matcher addMatcher = REGISTRY_ADD_PATTERN.matcher(chainingAddMatcher.group(0));
                    while (addMatcher.find()) {
                        if (addMatcher.groupCount() == 1 && addMatcher.group(1) != null) {
                            result.put(addMatcher.group(1), null);
                        }
                    }
                }
            }
            return result;
        };
    }

    @Override
    @NotNull
    public KeyDescriptor<String> getKeyDescriptor() {
        return EnumeratorStringDescriptor.INSTANCE;
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    @NotNull
    public FileBasedIndex.InputFilter getInputFilter() {
        return file -> {
            String extension = file.getExtension();
            return extension != null && extension.toLowerCase().equals("js");
        };
    }

    @Override
    public boolean dependsOnFileContent() {
        return true;
    }

    public static Collection<String> getAvailableWidgetNames(@NotNull GlobalSearchScope scope,
                                                             @NotNull Project project,
                                                             boolean withViewTypePrefix) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        Collection<String> allNames = index.getAllKeys(NAME, project);
        Set<String> names = new HashSet<>();
        for (String name : allNames) {
            if (name.contains(".") && !withViewTypePrefix) {
                String[] splits = name.split("\\.", 2);
                name = splits[1];
                if (names.contains(name)) {
                    continue;
                }
            }
            String finalName = name;
            index.processValues(NAME, name, null, (file, value) -> {
                names.add(finalName);
                return false;
            }, scope);
        }
        return names;
    }

    public static Collection<String> getAvailableWidgetNames(@NotNull PsiElement anchor,
                                                             boolean withViewTypePrefix) {
        GlobalSearchScope scope = OdooModuleUtils.getOdooModuleWithDependenciesOrSystemWideModulesScope(anchor);
        return getAvailableWidgetNames(scope, anchor.getProject(), withViewTypePrefix);
    }

    public static void processAvailableWidgets(@NotNull GlobalSearchScope scope,
                                               @NotNull Project project,
                                               @NotNull Processor<OdooJSFieldWidget> processor) {
        FileBasedIndex index = FileBasedIndex.getInstance();
        PsiManager psiManager = PsiManager.getInstance(project);
        Collection<String> allNames = index.getAllKeys(NAME, project);
        for (String name : allNames) {
            index.processValues(NAME, name, null, (file, value) -> {
                PsiFile psiFile = psiManager.findFile(file);
                if (psiFile != null) {
                    processor.process(new OdooJSFieldWidget(name, psiFile));
                }
                return true;
            }, scope);
        }
    }

    public static Collection<OdooJSFieldWidget> getWidgetsByName(@NotNull String name,
                                                                 @NotNull GlobalSearchScope scope,
                                                                 @NotNull Project project,
                                                                 boolean withViewTypePrefix) {
        List<OdooJSFieldWidget> widgets = new LinkedList<>();
        processAvailableWidgets(scope, project, widget -> {
            if ((withViewTypePrefix && name.equals(widget.getOriginName()))
                    || (!withViewTypePrefix && name.equals(widget.getName()))) {
                widgets.add(widget);
            }
            return true;
        });
        return widgets;
    }

    public static Collection<OdooJSFieldWidget> getWidgetsByName(@NotNull String name,
                                                                 @NotNull PsiElement anchor,
                                                                 boolean withViewTypePrefix) {
        GlobalSearchScope scope = OdooModuleUtils.getOdooModuleWithDependenciesOrSystemWideModulesScope(anchor);
        return getWidgetsByName(name, scope, anchor.getProject(), withViewTypePrefix);
    }
}
