package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.fieldtyper.FieldTyperTextField;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueText extends FieldValue {
    private String value;
    private FieldTyperTextField.TextEditorType editorType;

    public FieldValueText(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public boolean isFilled() {
        return value != null;
    }

    @Override
    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        FieldValueText clone = new FieldValueText(newTwinClassFieldEntity);
        clone
                .setValue(this.value)
                .setEditorType(this.editorType);
        return clone;
    }

    @Override
    public boolean hasValue(String value) {
        return StringUtils.equals(this.value, value);
    }

    public void nullify() {
        value = "";
        editorType = null;
    }

    @Override
    public boolean isNullified() {
        return "".equals(value) && editorType == null;
    }
}
