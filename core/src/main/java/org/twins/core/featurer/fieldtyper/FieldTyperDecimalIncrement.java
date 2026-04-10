package org.twins.core.featurer.fieldtyper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
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

    private static final Pattern INCREMENT_PATTERN = Pattern.compile("^(0|[+-]\\d+(\\.\\d+)?)$");

    @Override
    public FieldDescriptorNumeric getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorNumeric()
                .min(min.extract(properties))
                .max(max.extract(properties))
                .step(step.extract(properties))
                .thousandSeparator(thousandSeparator.extract(properties))
                .decimalSeparator(decimalSeparator.extract(properties))
                .decimalPlaces(decimalPlaces.extract(properties))
                .round(round.extract(properties))
                .extraThousandSeparators(extraThousandSeparatorSet.extract(properties))
                .extraDecimalSeparators(extraDecimalSeparatorSet.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinEntity twin, TwinFieldDecimalEntity twinFieldDecimalEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (value.isUndefined()) {
            return;
        }

        String rawValue = value.getValue();
        BigDecimal delta = parseIncrement(rawValue, value.getTwinClassField());

        TwinFieldDecimalEntity entity = twinFieldDecimalEntity != null ? twinFieldDecimalEntity :
                new TwinFieldDecimalEntity()
                        .setTwin(twin)
                        .setTwinId(twin.getId())
                        .setTwinClassFieldId(value.getTwinClassFieldId());

        entity
                .setIncrementOperation(true)
                .setValue(delta);
        twinChangesCollector.add(entity);
        addHistoryContext(twinChangesCollector, entity, delta);
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldDecimalEntity twinFieldDecimalEntity) throws ServiceException {
        return deserializeValueBase(properties, twinField, twinFieldDecimalEntity);
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

            // Validate delta using base validation (min/max, decimal places, etc.)
            // Create temporary FieldValueText with delta value for validation
            FieldValueText deltaValue = new FieldValueText(fieldValue.getTwinClassField())
                    .setValue(delta.toPlainString());

            try {
                processAndFormatValue(properties, deltaValue);
            } catch (ServiceException e) {
                return new ValidationResult(false, e.getMessage());
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
