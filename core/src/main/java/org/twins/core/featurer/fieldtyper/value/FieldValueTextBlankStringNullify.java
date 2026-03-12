package org.twins.core.featurer.fieldtyper.value;

import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class FieldValueTextBlankStringNullify extends FieldValueText {
    public FieldValueTextBlankStringNullify(TwinClassFieldEntity twinClassField) {
        super(twinClassField);
    }

    @Override
    public FieldValueTextBlankStringNullify setValue(String newValue) {
        return (FieldValueTextBlankStringNullify) super.setValue(StringUtils.isBlank(newValue) ? null : newValue);
    }

    @Override
    public FieldValueTextBlankStringNullify newInstance(TwinClassFieldEntity newTwinClassFieldEntity) {
        return new FieldValueTextBlankStringNullify(newTwinClassFieldEntity);
    }
}
