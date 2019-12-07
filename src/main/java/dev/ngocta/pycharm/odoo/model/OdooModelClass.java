package dev.ngocta.pycharm.odoo.model;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ModificationTracker;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.PsiElementBase;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.stubs.IStubElementType;
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
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.OdooUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OdooModelClass extends PsiElementBase implements PyClass {
    private final String myName;
    private final Project myProject;

    private OdooModelClass(@NotNull String name, @NotNull Project project) {
        myName = name;
        myProject = project;
    }

    public static OdooModelClass create(@NotNull String model, @NotNull Project project) {
        ConcurrentMap<String, OdooModelClass> registry = CachedValuesManager.getManager(project).getCachedValue(project, () -> {
            return CachedValueProvider.Result.create(new ConcurrentHashMap<>(), ModificationTracker.NEVER_CHANGED);
        });
        OdooModelClass cls = registry.get(model);
        if (cls == null) {
            cls = new OdooModelClass(model, project);
            registry.put(model, cls);
        }
        return cls;
    }

    @Nullable
    @Override
    public ASTNode getNameNode() {
        return null;
    }

    @NotNull
    @Override
    public List<PyClass> getAncestorClasses(@Nullable TypeEvalContext context) {
        return PyUtil.getParameterizedCachedValue(this, context, contextArg -> doGetAncestorClasses(context, true));
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
            PsiFile anchor = context.getOrigin();
            if (firstLevel && anchor != null) {
                PyClass baseClass = OdooUtils.getClassByQName(OdooNames.BASE_MODEL_CLASS_QNAME, anchor);
                if (baseClass != null) {
                    result.add(baseClass);
                }
            }
        }
        return result;
    }

    @NotNull
    @Override
    public List<PyClassLikeType> getSuperClassTypes(@NotNull TypeEvalContext context) {
        return new OdooModelClassType(this, OdooRecordSetType.NONE).getSuperClassTypes(context);
    }

    @NotNull
    @Override
    public PyClass[] getSuperClasses(@Nullable TypeEvalContext context) {
        if (context == null) {
            return new PyClass[0];
        }
        return PyUtil.getParameterizedCachedValue(this, context, contextArg -> {
            PyClass[] classes = new PyClass[0];
            if (contextArg == null) {
                return classes;
            }
            PsiFile file = contextArg.getOrigin();
            if (file == null) {
                return classes;
            }
            List<PyClass> modelClasses = OdooModelIndex.findModelClasses(getName(), file, true);
            List<String> superModels = new LinkedList<>();
            modelClasses.forEach(modelClass -> {
                OdooModelInfo info = OdooModelInfo.readFromClass(modelClass);
                if (info != null) {
                    info.getInherit().forEach(inherit -> {
                        if (!inherit.equals(getName())) {
                            superModels.add(0, inherit);
                        }
                    });
                }
            });
            superModels.stream().distinct().forEach(model -> {
                modelClasses.add(OdooModelClass.create(model, myProject));
            });
            return modelClasses.toArray(classes);
        });
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
    public OdooModelClassType getType(@NotNull TypeEvalContext context) {
        return new OdooModelClassType(this, OdooRecordSetType.NONE);
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
    public OdooModelClassType getType(@NotNull TypeEvalContext context, TypeEvalContext.@NotNull Key key) {
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
        return true;
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
        return new OdooModelClassType(myName, OdooRecordSetType.NONE, myProject).getAncestorTypes(context);
    }

    @NotNull
    public List<OdooModelClass> getDelegationChildren(@NotNull TypeEvalContext context) {
        Set<String> children = new HashSet<>();
        getAncestorClasses(context).forEach(cls -> {
            OdooModelInfo info = OdooModelInfo.readFromClass(cls);
            if (info != null) {
                children.addAll(info.getInherits().keySet());
            }
        });
        List<OdooModelClass> result = new LinkedList<>();
        children.forEach(child -> {
            result.add(create(child, myProject));
        });
        return result;
    }

    @Nullable
    public PyTargetExpression findField(@NotNull String name, @NotNull TypeEvalContext context) {
        PyTargetExpression attr = findClassAttribute(name, true, context);
        if (attr != null && OdooFieldInfo.getInfo(attr, context) != null) {
            return attr;
        }
        List<OdooModelClass> children = getDelegationChildren(context);
        for (OdooModelClass child : children) {
            PyTargetExpression field = child.findField(name, context);
            if (field != null) {
                return field;
            }
        }
        return null;
    }

    @Nullable
    public PyTargetExpression findFieldByPath(@NotNull String path, @NotNull TypeEvalContext context) {
        String[] names = path.split("\\.");
        return findFieldByPath(Arrays.asList(names), context);
    }

    @Nullable
    public PyTargetExpression findFieldByPath(@NotNull List<String> fieldNames, @NotNull TypeEvalContext context) {
        if (fieldNames.isEmpty()) {
            return null;
        }
        String name = fieldNames.get(0);
        PyTargetExpression field = findField(name, context);
        if (fieldNames.size() == 1) {
            return field;
        }
        if (field != null) {
            PyType fieldType = OdooFieldInfo.getFieldType(field, context);
            if (fieldType instanceof OdooModelClassType) {
                fieldNames = fieldNames.subList(1, fieldNames.size());
                return ((OdooModelClassType) fieldType).getPyClass().findFieldByPath(fieldNames, context);
            }
        }
        return null;
    }
}
