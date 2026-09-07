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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinService;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

/**
 * Backend field-value validators ({@code twin_class_field_validator}).
 * <p>
 * Batch-first contract: the public entry takes raw config params ({@link HashMap}) and a
 * {@link FieldValidateBatch} of items sharing the same {@code (featurerId, params)};
 * {@link #extractProperties} runs once per group. Implementers extend
 * {@link FieldValidatorAtomic} (preload in {@code beforeValidate}, in-memory per item) or override
 * {@link #isValidBatch(FieldValidateBatch, Properties)} directly for query-result strategies.
 */
@FeaturerType(id = FeaturerTwins.TYPE_56,
        name = "FieldValidator",
        description = "Backend validation of twin class field value")
@Slf4j
public abstract class FieldValidator extends FeaturerTwins {

    @Lazy
    @Autowired
    protected I18nService i18nService;

    @Lazy
    @Autowired
    protected TwinService twinService;

    /**
     * Public batch entry — params exactly as stored on {@link TwinClassFieldValidatorEntity}.
     * Extracts {@link Properties} once, then delegates.
     */
    public void isValidBatch(FieldValidateBatch batch, HashMap<String, String> fieldValidatorParams) throws ServiceException {
        if (batch.isEmpty())
            return;
        Properties properties = featurerService.extractProperties(this, fieldValidatorParams != null ? fieldValidatorParams : new HashMap<>());
        log.info("Running field validator[{}] for {} item(s) with params: {}", this.getClass().getSimpleName(), batch.getItems().size(), properties);
        isValidBatch(batch, properties);
        fillErrorMessages(batch);
    }

    public abstract void isValidBatch(FieldValidateBatch batch, Properties properties) throws ServiceException;

    /**
     * Single-item convenience for tests / non-batch callers. Extracts params and runs a one-item batch.
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
     * Resolves messages for failed items without a message yet.
     * <p>
     * Validation errors land in {@code invalidTwinFieldErrors} as plain strings on the exception,
     * so translations must be resolved here. Bulk {@link I18nService#translateToLocale(Set)} —
     * one query for the whole batch, not per-item {@code translateToLocale(UUID)}.
     */
    protected void fillErrorMessages(FieldValidateBatch batch) throws ServiceException {
        Set<UUID> i18nIds = new HashSet<>();
        for (FieldValidateItem item : batch.getItems()) {
            ValidationResult result = item.getResult();
            if (result == null || result.isValid() || StringUtils.isNotBlank(result.getMessage()))
                continue;
            UUID i18nId = item.getValidatorEntity().getBeValidationErrorI18nId();
            if (i18nId != null)
                i18nIds.add(i18nId);
        }
        Map<UUID, String> translations = i18nIds.isEmpty()
                ? Map.of()
                : i18nService.translateToLocale(i18nIds);
        for (FieldValidateItem item : batch.getItems()) {
            ValidationResult result = item.getResult();
            if (result == null || result.isValid() || StringUtils.isNotBlank(result.getMessage()))
                continue;
            result.setMessage(errorMessage(item.getValidatorEntity(), item.getValue(), translations));
        }
    }

    /**
     * Prefer the validator's configured i18n (from the preloaded {@code translations} map).
     * Fallback: generic field-incorrect message.
     */
    protected String errorMessage(TwinClassFieldValidatorEntity validatorEntity, FieldValue value, Map<UUID, String> translations) throws ServiceException {
        if (validatorEntity.getBeValidationErrorI18nId() != null) {
            String message = translations.get(validatorEntity.getBeValidationErrorI18nId());
            if (StringUtils.isNotBlank(message))
                return message;
        }
        return twinService.getErrorMessage(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, value.getTwinClassField());
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
