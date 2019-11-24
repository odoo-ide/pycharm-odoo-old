package dev.ngocta.pycharm.odoo;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.PsiElementBase;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.util.CachedValue;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.util.Processor;
import com.jetbrains.python.PyStubElementTypes;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.psi.*;
import com.jetbrains.python.psi.stubs.PyClassStub;
import com.jetbrains.python.psi.types.PyClassLikeType;
import com.jetbrains.python.psi.types.PyType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class OdooModelClass extends PsiElementBase implements PyClass {
    private String myName;
    private Project myProject;
    private final static Key<CachedValue<Map<String, OdooModelClass>>> REGISTRY_KEY = new Key<>("Registry");
    private static final Key<CachedValue<PyClass>> BASE_MODEL_KEY = Key.create("BaseModel");


    public static OdooModelClass get(@NotNull String model, @NotNull Project project) {
        Map<String, OdooModelClass> registry = CachedValuesManager.getManager(project).getCachedValue(project, REGISTRY_KEY, () -> {
            return CachedValueProvider.Result.create(new HashMap<>(), ModificationTracker.NEVER_CHANGED);
        }, false);
        return registry.getOrDefault(model, new OdooModelClass(model, project));
    }

    private OdooModelClass(@NotNull String name, @NotNull Project project) {
        myName = name;
        myProject = project;
    }

    @Nullable
    @Override
    public ASTNode getNameNode() {
        return null;
    }

    @NotNull
    @Override
    public List<PyClass> getAncestorClasses(@Nullable TypeEvalContext context) {
        return PyUtil.getParameterizedCachedValue(this, context, contextArg -> doGetAncestorClasses(contextArg, true));
    }

    @NotNull
    private List<PyClass> doGetAncestorClasses(@Nullable TypeEvalContext context, boolean firstLevel) {
        List<PyClass> result = new LinkedList<>();
        if (context != null) {
            PyClass[] classes = getSuperClasses(context);
            for (PyClass cls : classes) {
                if (cls instanceof OdooModelClass) {
                    result.addAll(((OdooModelClass) cls).doGetAncestorClasses(context, false));
                } else {
                    result.add(cls);
                }
            }
            if (firstLevel && context.getOrigin() != null) {
                PyClass baseClass = getBaseModelClass(context.getOrigin());
                if (baseClass != null) {
                    result.add(baseClass);
                }
            }
        }
        return result;
    }

    @Nullable
    private static PyClass getBaseModelClass(@NotNull PsiElement anchor) {
        Project project = anchor.getProject();
        return CachedValuesManager.getManager(project).getCachedValue(project, BASE_MODEL_KEY, () -> {
            PyClass cls = PyPsiFacade.getInstance(anchor.getProject()).createClassByQName(OdooNames.ODOO_MODELS_BASE_MODEL, anchor);
            return CachedValueProvider.Result.create(cls, cls);
        }, true);
    }

    @NotNull
    @Override
    public List<PyClassLikeType> getSuperClassTypes(@NotNull TypeEvalContext context) {
        return (new OdooModelClassType(this, null)).getSuperClassTypes(context);
    }

    @NotNull
    @Override
    public PyClass[] getSuperClasses(@Nullable TypeEvalContext context) {
        PyClass[] classes = new PyClass[0];
        if (context == null) {
            return classes;
        }
        PsiFile file = context.getOrigin();
        if (file == null) {
            return classes;
        }
        PsiDirectory module = OdooUtils.getOdooModuleDir(file);
        if (module == null) {
            return classes;
        }
        List<PyClass> classList = new LinkedList<>();
        List<PsiDirectory> modules = OdooModuleIndex.getFlattenedDependsGraph(module);
        List<String> superModels = new LinkedList<>();
        modules.forEach(mod -> {
            List<PyClass> modelClasses = OdooModelIndex.findModelClasses(getName(), mod);
            modelClasses.forEach(modelClass -> {
                classList.add(modelClass);
                OdooModelInfo info = OdooModelInfo.readFromClass(modelClass);
                if (info != null) {
                    info.getInherit().forEach(inherit -> {
                        if (!inherit.equals(getName())) {
                            superModels.add(0, inherit);
                        }
                    });
                }
            });
        });
        superModels.stream().distinct().forEach(model -> {
            classList.add(OdooModelClass.get(model, myProject));
        });
        return classList.toArray(classes);
    }

    @Nullable
    @Override
    public PyArgumentList getSuperClassExpressionList() {
        return null;
    }

    @NotNull
    @Override
    public PyExpression[] getSuperClassExpressions() {
        return new PyExpression[0];
    }

    @NotNull
    @Override
    public PyFunction[] getMethods() {
        return new PyFunction[0];
    }

    @NotNull
    @Override
    public Map<String, Property> getProperties() {
        return Collections.emptyMap();
    }

    @Nullable
    @Override
    public PyFunction findMethodByName(@Nullable String name, boolean inherited, TypeEvalContext context) {
        if (inherited) {
            for (PyClass cls : getAncestorClasses(context)) {
                PyFunction method = cls.findMethodByName(name, false, context);
                if (method != null) {
                    return method;
                }
            }
        }
        return null;
    }

    @NotNull
    @Override
    public List<PyFunction> multiFindMethodByName(@NotNull String name, boolean inherited, @Nullable TypeEvalContext context) {
        List<PyFunction> result = new LinkedList<>();
        if (inherited) {
            for (PyClass cls : getAncestorClasses(context)) {
                result.addAll(cls.multiFindMethodByName(name, false, context));
            }
        }
        return result;
    }

    @Nullable
    @Override
    public PyFunction findInitOrNew(boolean inherited, @Nullable TypeEvalContext context) {
        return null;
    }

    @NotNull
    @Override
    public List<PyFunction> multiFindInitOrNew(boolean inherited, @Nullable TypeEvalContext context) {
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public Property findProperty(@NotNull String name, boolean inherited, @Nullable TypeEvalContext context) {
        if (inherited) {
            for (PyClass cls : getAncestorClasses(context)) {
                Property prop = cls.findProperty(name, false, context);
                if (prop != null) {
                    return prop;
                }
            }
        }
        return null;
    }

    @Override
    public boolean visitMethods(Processor<PyFunction> processor, boolean inherited, @Nullable TypeEvalContext context) {
        if (inherited) {
            for (PyClass cls : getAncestorClasses(context)) {
                if (!cls.visitMethods(processor, true, context)) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public boolean visitClassAttributes(Processor<PyTargetExpression> processor, boolean inherited, TypeEvalContext context) {
        if (inherited) {
            for (PyClass cls : getAncestorClasses(context)) {
                if (!cls.visitClassAttributes(processor, true, context)) {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public List<PyTargetExpression> getClassAttributes() {
        return Collections.emptyList();
    }

    @NotNull
    @Override
    public List<PyTargetExpression> getClassAttributesInherited(@NotNull TypeEvalContext context) {
        List<PyTargetExpression> result = new LinkedList<>();
        for (PyClass cls : getAncestorClasses(context)) {
            result.addAll(cls.getClassAttributes());
        }
        return result;
    }

    @Nullable
    @Override
    public PyTargetExpression findClassAttribute(@NotNull String name, boolean inherited, TypeEvalContext context) {
        if (inherited) {
            for (PyClass cls : getAncestorClasses(context)) {
                PyTargetExpression attr = cls.findClassAttribute(name, false, context);
                if (attr != null) {
                    return attr;
                }
            }
        }
        return null;
    }

    @Override
    public List<PyTargetExpression> getInstanceAttributes() {
        return null;
    }

    @Nullable
    @Override
    public PyTargetExpression findInstanceAttribute(String name, boolean inherited) {
        return null;
    }

    @Override
    public PyClass[] getNestedClasses() {
        return new PyClass[0];
    }

    @Nullable
    @Override
    public PyClass findNestedClass(String name, boolean inherited) {
        return null;
    }

    @Override
    public boolean isNewStyleClass(TypeEvalContext context) {
        return true;
    }

    @Nullable
    @Override
    public Property scanProperties(Processor<Property> processor, boolean inherited) {
        return null;
    }

    @Nullable
    @Override
    public Property findPropertyByCallable(PyCallable callable) {
        return null;
    }

    @Override
    public boolean isSubclass(PyClass parent, @Nullable TypeEvalContext context) {
        return this.getAncestorClasses(context).contains(parent);
    }

    @Override
    public boolean isSubclass(@NotNull String superClassQName, @Nullable TypeEvalContext context) {
        return this.getAncestorClasses(context).stream().anyMatch(cls -> superClassQName.equals(cls.getQualifiedName()));
    }

    @Nullable
    @Override
    public List<String> getSlots(@Nullable TypeEvalContext context) {
        return null;
    }

    @Nullable
    @Override
    public List<String> getOwnSlots() {
        return null;
    }

    @Nullable
    @Override
    public String getDocStringValue() {
        return null;
    }

    @Nullable
    @Override
    public StructuredDocString getStructuredDocString() {
        return null;
    }

    @Nullable
    @Override
    public PyStringLiteralExpression getDocStringExpression() {
        return null;
    }

    @Override
    public boolean processClassLevelDeclarations(@NotNull PsiScopeProcessor psiScopeProcessor) {
        return true;
    }

    @Override
    public boolean processInstanceLevelDeclarations(@NotNull PsiScopeProcessor psiScopeProcessor, @Nullable PsiElement psiElement) {
        return true;
    }

    @Nullable
    @Override
    public PyType getMetaClassType(@NotNull TypeEvalContext context) {
        return null;
    }

    @Nullable
    @Override
    public PyExpression getMetaClassExpression() {
        return null;
    }

    @Nullable
    @Override
    public PyClassLikeType getType(@NotNull TypeEvalContext context) {
        return OdooModelClassType.create(this, true);
    }

    @Nullable
    @Override
    public PsiElement getNameIdentifier() {
        return this;
    }

    @NotNull
    @Override
    public String getName() {
        return myName;
    }

    @Override
    public PsiElement setName(@NotNull String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IStubElementType getElementType() {
        return PyStubElementTypes.CLASS_DECLARATION;
    }

    @Nullable
    @Override
    public PyClassStub getStub() {
        return null;
    }

    @Nullable
    @Override
    public String getQualifiedName() {
        return myName;
    }

    @NotNull
    @Override
    public PyStatementList getStatementList() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public PyType getType(@NotNull TypeEvalContext context, TypeEvalContext.@NotNull Key key) {
        return getType(context);
    }

    @NotNull
    @Override
    public Project getProject() {
        return myProject;
    }

    @NotNull
    @Override
    public Language getLanguage() {
        return PythonFileType.INSTANCE.getLanguage();
    }

    @NotNull
    @Override
    public PsiElement[] getChildren() {
        return new PsiElement[0];
    }

    @Nullable
    @Override
    public PsiElement getParent() {
        return null;
    }

    @Nullable
    @Override
    public PsiFile getContainingFile() {
        return null;
    }

    @Override
    public TextRange getTextRange() {
        return null;
    }

    @Override
    public int getStartOffsetInParent() {
        return 0;
    }

    @Override
    public int getTextLength() {
        return 0;
    }

    @Nullable
    @Override
    public PsiElement findElementAt(int offset) {
        return null;
    }

    @Nullable
    @Override
    public PsiReference findReferenceAt(int offset) {
        return null;
    }

    @Override
    public int getTextOffset() {
        return 0;
    }

    @Override
    public String getText() {
        return "";
    }

    @NotNull
    @Override
    public char[] textToCharArray() {
        return new char[0];
    }

    @Override
    public boolean isValid() {
        return OdooModelIndex.checkModelExists(myName, myProject);
    }

    @Override
    public ASTNode getNode() {
        return null;
    }

    @Nullable
    @Override
    public PyDecoratorList getDecoratorList() {
        return null;
    }

    @NotNull
    @Override
    public List<PyClassLikeType> getAncestorTypes(@NotNull TypeEvalContext context) {
        return (new OdooModelClassType(this, null)).getAncestorTypes(context);
    }
}
