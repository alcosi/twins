package org.twins.core.featurer.fieldvalidator;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.validator.TwinClassFieldValidatorEntity;
import org.twins.core.domain.twinclass.FieldValidateBatch;
import org.twins.core.domain.twinclass.FieldValidateItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.twin.TwinService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * Backend field-value validators ({@code twin_class_field_validator}).
 * <p>
 * Batch-first contract: the public entry takes raw config params ({@link HashMap}) and a
 * {@link FieldValidateBatch} of items sharing the same {@code (featurerId, params)};
 * {@link #extractProperties} runs once per group. Implementers extend
 * {@link FieldValidatorAtomic} (preload in {@code beforeValidate}, in-memory per item) or override
 * {@link #isValidBatch(FieldValidateBatch, Properties)} directly for query-result strategies.
 * <p>
 * On failure, {@link ValidationResult#messageI18nId} is attached from the validator config
 * (no DB). Translations are resolved once for all groups by
 * {@link org.twins.core.service.twinclassfield.TwinClassFieldValidatorService#validateFieldValues}.
 */
@FeaturerType(id = FeaturerTwins.TYPE_56,
        name = "FieldValidator",
        description = "Backend validation of twin class field value")
@Slf4j
public abstract class FieldValidator extends FeaturerTwins {

    @Lazy
    @Autowired
    protected TwinService twinService;

    /**
     * Public batch entry — params exactly as stored on {@link TwinClassFieldValidatorEntity}.
     * Extracts {@link Properties} once, then delegates. Attaches {@code messageI18nId} on
     * failures; does not translate (caller bulk-resolves after all groups).
     */
    public void isValidBatch(FieldValidateBatch batch, HashMap<String, String> fieldValidatorParams) throws ServiceException {
        if (batch.isEmpty())
            return;
        Properties properties = featurerService.extractProperties(this, fieldValidatorParams != null ? fieldValidatorParams : new HashMap<>());
        log.info("Running field validator[{}] for {} item(s) with params: {}", this.getClass().getSimpleName(), batch.getItems().size(), properties);
        isValidBatch(batch, properties);
        attachMessageI18nIds(batch.getItems());
    }

    public abstract void isValidBatch(FieldValidateBatch batch, Properties properties) throws ServiceException;

    /**
     * Single-item convenience for tests / non-batch callers.
     * Attaches {@code messageI18nId} on failure; does not resolve {@code message} (no i18n bulk here).
     */
    public ValidationResult isValid(TwinClassFieldValidatorEntity validatorEntity, TwinEntity twinEntity, FieldValue value, Map<UUID, FieldValue> contextFields) throws ServiceException {
        FieldValidateItem item = new FieldValidateItem()
                .setValidatorEntity(validatorEntity)
                .setTwinEntity(twinEntity)
                .setValue(value)
                .setContextFields(contextFields);
        FieldValidateBatch batch = new FieldValidateBatch().add(item);
        isValidBatch(batch, validatorEntity.getFieldValidatorParams());
        return item.getResult() != null ? item.getResult() : ValidationResult.VALID;
    }

    /**
     * Copies {@code beValidationErrorI18nId} onto failed results that have neither a ready
     * {@code message} nor {@code messageI18nId}. No DB access.
     */
    protected void attachMessageI18nIds(Collection<FieldValidateItem> items) {
        for (FieldValidateItem item : items) {
            ValidationResult result = item.getResult();
            if (result == null || result.isValid())
                continue;
            if (result.getMessageI18nId() != null || StringUtils.isNotBlank(result.getMessage()))
                continue;
            UUID i18nId = item.getValidatorEntity().getBeValidationErrorI18nId();
            if (i18nId != null)
                result.setMessageI18nId(i18nId);
        }
    }

    /**
     * Resolves another twin class field value for cross-field validation.
     * Payload wins; an explicitly empty payload value means there is nothing to compare yet.
     * DB fallback reads {@link TwinEntity#getFieldValuesKit()} — must already be preloaded
     * in {@code beforeValidate} (no DB access here).
     */
    protected FieldValue resolveFieldValue(TwinEntity twinEntity, Map<UUID, FieldValue> contextFields, UUID twinClassFieldId) {
        if (contextFields != null && contextFields.containsKey(twinClassFieldId)) {
            FieldValue fieldValue = contextFields.get(twinClassFieldId);
            return fieldValue == null || fieldValue.isEmpty() ? null : fieldValue;
        }
        if (twinEntity.getFieldValuesKit() == null)
            return null;
        return twinEntity.getFieldValuesKit().get(twinClassFieldId);
    }
}
