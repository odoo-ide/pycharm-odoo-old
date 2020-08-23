package dev.ngocta.pycharm.odoo.xml.dom;

import com.intellij.util.xml.*;
import dev.ngocta.pycharm.odoo.OdooNames;
import dev.ngocta.pycharm.odoo.data.OdooRecordExtraInfo;
import dev.ngocta.pycharm.odoo.data.OdooRecordViewInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface OdooDomRecord extends OdooDomRecordLike {
    @Attribute("model")
    @Required
    @Referencing(OdooModelReferenceConverter.class)
    GenericAttributeValue<String> getModelAttribute();

    @SubTag("field")
    List<OdooDomFieldAssignment> getFields();

    @Nullable
    default OdooDomFieldAssignment findField(@NotNull String name) {
        List<OdooDomFieldAssignment> fields = getFields();
        for (OdooDomFieldAssignment field : fields) {
            if (name.equals(field.getName())) {
                return field;
            }
        }
        return null;
    }

    @Override
    @Nullable
    default String getModel() {
        return getModelAttribute().getStringValue();
    }

    @Override
    @Nullable
    default OdooRecordExtraInfo getRecordExtraInfo() {
        if (OdooNames.IR_UI_VIEW.equals(getModel())) {
            OdooDomFieldAssignment modelField = findField("model");
            String viewModel = modelField != null ? modelField.getStringValue() : null;
            OdooDomFieldAssignment inheritField = findField("inherit_id");
            String inheritId = inheritField != null ? inheritField.getRefAttr().getStringValue() : null;
            return new OdooRecordViewInfo(null, viewModel, inheritId);
        }
        return null;
    }
}
