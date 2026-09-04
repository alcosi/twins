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
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.service.i18n.I18nService;
import org.twins.core.service.twin.TwinService;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

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

    public ValidationResult isValid(TwinClassFieldValidatorEntity validatorEntity, TwinEntity twinEntity, FieldValue value, Map<UUID, FieldValue> contextFields) throws ServiceException {
        if (value.isEmpty()) // validators are applied only to filled values
            return ValidationResult.VALID;
        Properties properties = featurerService.extractProperties(this, validatorEntity.getFieldValidatorParams());
        log.info("Running field validator[{}] for {} with params: {}", this.getClass().getSimpleName(), value.getTwinClassField().logNormal(), properties);
        ValidationResult validationResult = isValid(properties, twinEntity, value, contextFields);
        if (!validationResult.isValid() && StringUtils.isBlank(validationResult.getMessage()))
            validationResult.setMessage(errorMessage(validatorEntity, value));
        return validationResult;
    }

    protected abstract ValidationResult isValid(Properties properties, TwinEntity twinEntity, FieldValue value, Map<UUID, FieldValue> contextFields) throws ServiceException;

    protected String errorMessage(TwinClassFieldValidatorEntity validatorEntity, FieldValue value) throws ServiceException {
        if (validatorEntity.getBeValidationErrorI18nId() != null) {
            String message = i18nService.translateToLocale(validatorEntity.getBeValidationErrorI18nId());
            if (StringUtils.isNotBlank(message))
                return message;
        }
        return twinService.getErrorMessage(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, value.getTwinClassField());
    }

    /**
     * Resolves the value of another twin class field for cross-field validation.
     * The payload value wins: an explicitly empty value in the payload means there is nothing to compare yet.
     * Otherwise, the value is loaded from the database (idempotent).
     */
    protected FieldValue resolveFieldValue(TwinEntity twinEntity, Map<UUID, FieldValue> contextFields, UUID twinClassFieldId) throws ServiceException {
        if (contextFields != null && contextFields.containsKey(twinClassFieldId)) {
            FieldValue fieldValue = contextFields.get(twinClassFieldId);
            return fieldValue == null || fieldValue.isEmpty() ? null : fieldValue;
        }
        twinService.loadFieldsValues(twinEntity);
        return twinEntity.getFieldValuesKit().get(twinClassFieldId);
    }
}
