package org.twins.core.featurer.fieldtyper.value;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.ValidationResult;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

/**
 * FieldValue class is not combined with TwinField class, because in some cases
 * we need to have values out of twin (for example, in a transition context)
 */
@Accessors(chain = true)
public abstract class FieldValue implements Cloneable {
    @Getter
    protected final TwinClassFieldEntity twinClassField;

    @Getter
    @Setter
    private ValidationResult validationResult;

    @Getter
    @Setter
    //will help to prevent repeated initialization and identify values set by a system
    private boolean systemInitialized = false;

    public FieldValue(TwinClassFieldEntity twinClassField) {
        this.twinClassField = twinClassField;
    }

    public boolean isBaseField() {
        return twinClassField.isBaseField();
    }

    public UUID getTwinClassFieldId() {
        return twinClassField.getId();
    }

    public FieldValue clone() {
        return clone(twinClassField);
    }

    public FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity) {
        var clone = newInstance(newTwinClassFieldEntity);
        copyValueTo(clone);
        return clone;
    }

    public abstract FieldValue newInstance(TwinClassFieldEntity newTwinClassFieldEntity);

    public abstract boolean hasValue(String value);

    public boolean isValidated() {
        return validationResult != null;
    }

    public boolean isEmpty() {
        return isUndefined() || isCleared();
    }

    public boolean isNotEmpty() {
        return !isEmpty();
    }

    public abstract void copyValueTo(FieldValue dst);

    public abstract FieldValue undefine();

    public abstract boolean isUndefined();

    public abstract FieldValue clear();

    public abstract boolean isCleared();
}
