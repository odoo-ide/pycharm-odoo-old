package dev.ngocta.pycharm.odoo.python.field;

import com.jetbrains.python.psi.types.PyType;

public interface OdooFieldType extends PyType {
    OdooFieldInfo getFieldInfo();
}
