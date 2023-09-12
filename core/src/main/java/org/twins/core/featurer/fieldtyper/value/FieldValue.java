package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@Data
@Accessors(chain = true)
public abstract class FieldValue {
    private TwinClassFieldEntity twinClassField;
}
