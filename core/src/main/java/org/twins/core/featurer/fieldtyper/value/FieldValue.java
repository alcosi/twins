package org.twins.core.featurer.fieldtyper.value;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.ValidationResult;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;

import java.util.UUID;

/**
 * FieldValue class is not combined with TwinField class, because ins some cases
 * we need to have values out of twin (for example, in transition context)
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
    private boolean alreadyInitialized = false; //will help to prevent repeated initialization
    protected State state = State.UNDEFINED;

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
        var clone = clone(twinClassField);
        clone.state = state;
        return clone;
    }

    public abstract FieldValue clone(TwinClassFieldEntity newTwinClassFieldEntity);

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

    public abstract void copyValueFrom(FieldValue src);

    public enum State {
        UNDEFINED,
        PRESENT,
        CLEARED
    }

    public FieldValue undefine() {
        this.state = State.UNDEFINED;
        onUndefine();
        return this;
    }

    public abstract void onUndefine();

    public boolean isUndefined() {
        updateMutableValueState();
        return this.state == State.UNDEFINED;
    }

    protected void updateMutableValueState() {
        //override me if state is depend upon some mutable data
    }

    public FieldValue clear() {
        this.state =  State.CLEARED;
        onClear();
        return this;
    }

    public abstract void onClear();

    public boolean isCleared() {
        updateMutableValueState();
        return this.state == State.CLEARED;
    }
}
