package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

@Data
@Accessors(chain = true)
public abstract class FieldValue implements Cloneable{
    private TwinClassFieldEntity twinClassField;

    public abstract FieldValue clone();

    public abstract boolean hasValue(String value);
}
