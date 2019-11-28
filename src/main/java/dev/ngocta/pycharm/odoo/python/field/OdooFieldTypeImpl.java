package dev.ngocta.pycharm.odoo.python.field;

import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.types.PyClassType;
import com.jetbrains.python.psi.types.PyClassTypeImpl;
import org.jetbrains.annotations.NotNull;

public class OdooFieldTypeImpl extends PyClassTypeImpl implements OdooFieldType {
    public OdooFieldTypeImpl(@NotNull PyClass source, boolean isDefinition) {
        super(source, isDefinition);
    }

    @Override
    public OdooFieldInfo getFieldInfo() {
        return null;
    }
}
