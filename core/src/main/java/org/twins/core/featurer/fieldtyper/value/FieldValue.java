package org.twins.core.featurer.fieldtyper.value;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.UUID;

/**
 * FieldValue class is not combined with TwinField class, because in some cases
 * we need to have values out of twin (for example, in a transition context)
 */

/* TODO
    All children use @EqualsAndHashCode(callSuper = true), but FieldValue / FieldValueStated /
    FieldValueCollection have NO @EqualsAndHashCode, so the chain bottoms out at Object.equals (identity):
    no two distinct instances are ever equal. Hence clone() (newInstance + copyValueTo) never equals its
    original — see the @Disabled clone_* tests. Related: copyValueTo itself is value-only at the typed level,
    state is applied by copyValueTo(FieldValue) — see the TODO on FieldValueStated.copyValueTo(FieldValueStated).
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

    public ValidationResult initValidationResult(ValidationResult validationResult) {
        this.validationResult = validationResult;
        return this.validationResult;
    }

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

    public boolean isDefined() {
        return !isUndefined();
    }

    /**
     * Lookuper contract guard: a not-found lookup arrives as an undefined value, and a filler that
     * has no default for that scenario fails the item itself (the lookuper no longer throws).
     */
    public FieldValue assertIsDefined(String onUndefinedMsg) throws ServiceException {
        if (isUndefined())
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, onUndefinedMsg);
        return this;
    }

    /** Null-tolerant variant for paths that bypass the batch conversion (per-item UUID convenience). */
    public static FieldValue assertIsDefined(FieldValue fieldValue, String onUndefinedMsg) throws ServiceException {
        if (fieldValue == null)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, onUndefinedMsg);
        return fieldValue.assertIsDefined(onUndefinedMsg);
    }

    public abstract void copyValueTo(FieldValue dst);

    public abstract FieldValue undefine();

    public abstract boolean isUndefined();

    public abstract FieldValue clear();

    public abstract boolean isCleared();
}
