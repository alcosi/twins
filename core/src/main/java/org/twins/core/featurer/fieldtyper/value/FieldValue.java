package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

@Data
@Accessors(chain = true)
public abstract class FieldValue implements Cloneable {
    protected final TwinClassFieldEntity twinClassField;
    protected final boolean filled;

    public FieldValue(TwinClassFieldEntity twinClassField, boolean filled) {
        this.twinClassField = twinClassField;
        this.filled = filled;
    }

    public UUID getTwinClassFieldId() {
        return twinClassField.getId();
    }

    public abstract FieldValue clone();

    public abstract boolean hasValue(String value);
}
