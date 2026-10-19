package org.twins.core.featurer.fieldvalidator;

import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.twins.core.domain.twinclass.FieldValidateBatch;
import org.twins.core.domain.twinclass.FieldValidateItem;

import java.util.Properties;

/**
 * Per-item {@link FieldValidator}: batch is a single {@link #beforeValidate} hook (bulk-load)
 * followed by {@link #isValid} for each item. Concrete validators extend this and implement
 * {@link #isValid}; those that need relations override {@link #beforeValidate} and use
 * {@link FieldValidateBatch#getTwins()} / {@link FieldValidateBatch#getHeadTwins()}.
 * <p>
 * The per-item method must run entirely in-memory — no DB access.
 */
public abstract class FieldValidatorAtomic extends FieldValidator {

    @Override
    public final void isValidBatch(FieldValidateBatch batch, Properties properties) throws ServiceException {
        if (batch.isEmpty())
            return;
        beforeValidate(batch, properties);
        for (FieldValidateItem item : batch.getItems()) {
            if (item.getValue() == null || item.getValue().isEmpty()) {
                item.setResult(ValidationResult.VALID); // validators apply only to filled values
                continue;
            }
            item.setResult(isValid(properties, item));
        }
    }

    /**
     * Override to bulk-load relations needed by {@link #isValid} across the whole batch (default: no-op).
     */
    protected void beforeValidate(FieldValidateBatch batch, Properties properties) throws ServiceException {
    }

    protected abstract ValidationResult isValid(Properties properties, FieldValidateItem item) throws ServiceException;
}
