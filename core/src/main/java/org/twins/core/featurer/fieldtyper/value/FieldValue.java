package org.twins.core.featurer.fieldtyper.value;

import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

/**
 * FieldValue class is not combined with TwinField class, because ins some cases
 * we need to have values out of twin (for example, in transition context)
 */
@Data
@Accessors(chain = true)
public abstract class FieldValue implements Cloneable {
    protected final TwinClassFieldEntity twinClassField;

    public FieldValue(TwinClassFieldEntity twinClassField) {
        this.twinClassField = twinClassField;
    }

    public abstract boolean isFilled();

    public UUID getTwinClassFieldId() {
        return twinClassField.getId();
    }

    public FieldValue clone() {
        return clone(twinClassField);
    }

    public abstract FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity);

    public abstract void clear();

    public abstract boolean hasValue(String value);
}
