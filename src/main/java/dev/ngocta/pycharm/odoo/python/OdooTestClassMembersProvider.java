package dev.ngocta.pycharm.odoo.python;

import com.intellij.psi.PsiElement;
import com.jetbrains.python.codeInsight.PyCustomMember;
import com.jetbrains.python.codeInsight.controlflow.ScopeOwner;
import com.jetbrains.python.codeInsight.dataflow.scope.ScopeUtil;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyFunction;
import com.jetbrains.python.psi.PyTargetExpression;
import com.jetbrains.python.psi.impl.PyClassImpl;
import com.jetbrains.python.psi.types.PyClassMembersProviderBase;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.TypeEvalContext;
import dev.ngocta.pycharm.odoo.OdooNames;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class OdooTestClassMembersProvider extends PyClassMembersProviderBase {
    @Override
    @NotNull
    public Collection<PyCustomMember> getMembers(PyClassType classType, PsiElement location, @NotNull TypeEvalContext context) {
        PyClass cls = classType.getPyClass();
        if (location == null) {
            return Collections.emptyList();
        }
        if (classType.isDefinition() && cls.isSubclass(OdooNames.TEST_BASE_CASE_QNAME, context)) {
            List<PyCustomMember> members = new LinkedList<>();
            ScopeOwner scopeOwner = ScopeUtil.getScopeOwner(location);
            List<PyFunction> setUpClassMethods = cls.multiFindMethodByName("setUpClass", true, context);
            if (scopeOwner instanceof PyFunction) {
                setUpClassMethods.remove(scopeOwner);
            }
            Map<String, PyTargetExpression> attributesInSetUpClassMethods = new HashMap<>();
            for (PyFunction method : setUpClassMethods) {
                PyClassImpl.collectInstanceAttributes(method, attributesInSetUpClassMethods);
            }
            for (PyTargetExpression attr : attributesInSetUpClassMethods.values()) {
                if (attr.getName() != null) {
                    members.add(new PyCustomMember(attr.getName(), attr));
                }
            }
            return members;
        }
        return Collections.emptyList();
    }
}
