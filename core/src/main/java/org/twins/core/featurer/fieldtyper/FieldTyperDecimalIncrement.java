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
import org.cambium.featurer.params.FeaturerParamInt;
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

    private static final Pattern INCREMENT_PATTERN = Pattern.compile("^(0|[+-]\\d+(\\.\\d+)?)$");

    @FeaturerParam(name = "Decimal places", description = "Number of decimal places", order = 1)
    public static final FeaturerParamInt decimalPlaces = new FeaturerParamInt("decimalPlaces");

    @FeaturerParam(name = "Allow negative result", description = "Allow value to go below zero after decrement", order = 2, optional = true, defaultValue = "false")
    public static final FeaturerParamBoolean allowNegativeResult = new FeaturerParamBoolean("allowNegativeResult");

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric()
                .decimalPlaces(decimalPlaces.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldDecimalEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.isUndefined()) {
            return;
        }

        String rawValue = value.getValue();
        BigDecimal delta = parseIncrement(rawValue, value.getTwinClassField());

        // If field doesn't exist yet, create it via collector
        if (twinFieldDecimalEntity == null) {
            TwinFieldDecimalEntity entity = new TwinFieldDecimalEntity()
                    .setTwin(twin)
                    .setTwinId(twin.getId())
                    .setTwinClassFieldId(value.getTwinClassFieldId())
                    .setValue(delta);
            twinChangesCollector.add(entity);
            addHistoryContext(twinChangesCollector, entity, delta);
            return;
        }

        // Use atomic increment at DB level for existing fields
        twinFieldDecimalRepository.incrementValue(
                twin.getId(),
                value.getTwinClassFieldId(),
                delta
        );
        // Reload to get the new value from DB
        twinService.loadTwinFields(twin);
        TwinFieldDecimalEntity entity = twin.getTwinFieldDecimalKit().get(value.getTwinClassFieldId());
        BigDecimal newValue = entity.getValue();
        twinChangesCollector.add(entity);
        addHistoryContext(twinChangesCollector, entity, newValue);
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

        // Validate format: +N or -N
        if (!INCREMENT_PATTERN.matcher(rawValue).matches()) {
            return new ValidationResult(false, "Value must be in format: +N or -N (e.g. +1, -5)");
        }

        try {
            BigDecimal delta = parseIncrement(rawValue, fieldValue.getTwinClassField());

            // Check negative result for existing twins only
            if (!twin.isSketch()) {
                twinService.loadTwinFields(twin);
                TwinFieldDecimalEntity currentEntity = twin.getTwinFieldDecimalKit().get(fieldValue.getTwinClassFieldId());
                if (currentEntity != null && currentEntity.getValue() != null) {
                    BigDecimal resultValue = currentEntity.getValue().add(delta);
                    boolean allowNegative = Boolean.TRUE.equals(allowNegativeResult.extract(properties));
                    if (!allowNegative && resultValue.compareTo(BigDecimal.ZERO) < 0) {
                        return new ValidationResult(false, twinService.getErrorMessage(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, fieldValue.getTwinClassField()) + " Result would be negative");
                    }
                }
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
