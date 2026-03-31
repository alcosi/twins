package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamDouble;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldDecimalEntity;
import org.twins.core.dao.twin.TwinFieldDecimalRepository;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldValueSearchNumeric;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorNumeric;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
@Featurer(
        id = FeaturerTwins.ID_1350,
        name = "Decimal Increment",
        description = "Decimal field with atomic increment/decrement support (+N/-N format)"
)
public class FieldTyperDecimalIncrement extends FieldTyperDecimalBase<FieldDescriptorNumeric, FieldValueText, TwinFieldValueSearchNumeric> {

    private final TwinFieldDecimalRepository twinFieldDecimalRepository;

    private static final Pattern INCREMENT_PATTERN = Pattern.compile("^[+-]\\d+(\\.\\d+)?$");

    @FeaturerParam(name = "Min", description = "Min possible value", order = 1)
    public static final FeaturerParamDouble min = new FeaturerParamDouble("min");

    @FeaturerParam(name = "Max", description = "Max possible value", order = 2)
    public static final FeaturerParamDouble max = new FeaturerParamDouble("max");

    @FeaturerParam(name = "Decimal places", description = "Number of decimal places", order = 3)
    public static final FeaturerParamInt decimalPlaces = new FeaturerParamInt("decimalPlaces");

    @FeaturerParam(name = "Allow negative result", description = "Allow value to go below zero after decrement", order = 4, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean allowNegativeResult = new FeaturerParamBoolean("allowNegativeResult");

    @FeaturerParam(name = "Round", description = "Round a number to the required number of decimal places", order = 5, optional = true, defaultValue = "true")
    public static final FeaturerParamString round = new FeaturerParamString("round");

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric()
                .min(min.extract(properties))
                .max(max.extract(properties))
                .decimalPlaces(decimalPlaces.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldDecimalEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.isUndefined()) {
            return;
        }

        String rawValue = value.getValue();
        BigDecimal delta = parseIncrement(rawValue, value.getTwinClassField());

        // Perform atomic increment at DB level
        Integer updatedCount = twinFieldDecimalRepository.incrementValue(
                twin.getId(),
                value.getTwinClassFieldId(),
                delta
        );

        if (updatedCount == null || updatedCount == 0) {
            // Record doesn't exist - create it with the delta value
            twinFieldDecimalEntity = twinService.createTwinFieldDecimalEntity(twin, value.getTwinClassField(), delta);
            twinChangesCollector.add(twinFieldDecimalEntity);
        }

        // Reload the entity to get the new value for history
        twinService.loadTwinFields(twin);
        TwinFieldDecimalEntity updatedEntity = twin.getTwinFieldDecimalKit().get(value.getTwinClassFieldId());
        if (updatedEntity != null && twinChangesCollector.isHistoryCollectorEnabled()) {
            addHistoryContext(twinChangesCollector, updatedEntity, updatedEntity.getValue());
        }
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldDecimalEntity) {
        var scale = decimalPlaces.extract(properties);
        var value = "";

        if (scale != null) {
            value = twinFieldDecimalEntity != null && twinFieldDecimalEntity.getValue() != null
                    ? twinFieldDecimalEntity.getValue().setScale(scale, java.math.RoundingMode.UNNECESSARY).toPlainString()
                    : null;
        } else {
            value = twinFieldDecimalEntity != null && twinFieldDecimalEntity.getValue() != null
                    ? twinFieldDecimalEntity.getValue().toString()
                    : null;
        }

        return new FieldValueText(twinField.getTwinClassField()).setValue(value);
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldValueSearchNumeric search) throws ServiceException {
        return TwinSpecification.checkFieldDecimal(search);
    }

    @Override
    protected ValidationResult validate(Properties properties, TwinEntity twin, FieldValueText fieldValue) {
        String rawValue = fieldValue.getValue();

        if (StringUtils.isEmpty(rawValue)) {
            return ValidationResult.VALID;
        }

        // Validate format: must start with + or -
        if (!INCREMENT_PATTERN.matcher(rawValue).matches()) {
            return new ValidationResult(false, i18nService.translateToLocale(
                    fieldValue.getTwinClassField().getBeValidationErrorI18nId()));
        }

        try {
            BigDecimal delta = parseIncrement(rawValue, fieldValue.getTwinClassField());

            // Validate bounds if min/max are set
            var minValue = min.extract(properties);
            var maxValue = max.extract(properties);

            // Get current value to check if result will be within bounds
            twinService.loadTwinFields(twin);
            TwinFieldDecimalEntity currentEntity = twin.getTwinFieldDecimalKit().get(fieldValue.getTwinClassFieldId());
            BigDecimal currentValue = currentEntity != null && currentEntity.getValue() != null
                    ? currentEntity.getValue()
                    : BigDecimal.ZERO;

            BigDecimal resultValue = currentValue.add(delta);

            if (minValue != null && resultValue.doubleValue() < minValue) {
                return new ValidationResult(false,
                        twinService.getErrorMessage(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                                fieldValue.getTwinClassField()) + " Result would be below min value");
            }

            if (maxValue != null && resultValue.doubleValue() > maxValue) {
                return new ValidationResult(false,
                        twinService.getErrorMessage(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                                fieldValue.getTwinClassField()) + " Result would exceed max value");
            }

            boolean allowNegative = Boolean.TRUE.equals(allowNegativeResult.extract(properties));
            if (!allowNegative && resultValue.compareTo(BigDecimal.ZERO) < 0) {
                return new ValidationResult(false,
                        twinService.getErrorMessage(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                                fieldValue.getTwinClassField()) + " Result would be negative");
            }

        } catch (ServiceException e) {
            return new ValidationResult(false, e.getMessage());
        }

        return ValidationResult.VALID;
    }

    private BigDecimal parseIncrement(String rawValue, TwinClassFieldEntity twinClassFieldEntity) throws ServiceException {
        if (StringUtils.isEmpty(rawValue)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) + " value is empty");
        }

        if (!INCREMENT_PATTERN.matcher(rawValue).matches()) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) +
                            " value[" + rawValue + "] must be in format +N or -N");
        }

        try {
            return new BigDecimal(rawValue);
        } catch (NumberFormatException e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    twinClassFieldEntity.easyLog(EasyLoggable.Level.NORMAL) +
                            " value[" + rawValue + "] is not a valid number");
        }
    }
}
