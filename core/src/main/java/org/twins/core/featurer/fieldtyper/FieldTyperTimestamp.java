package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldTimestampEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchTimestamp;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorTimestamp;
import org.twins.core.featurer.fieldtyper.value.FieldValueTimestamp;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;

@Component
@Slf4j
@Featurer(id = FeaturerTwins.ID_1349, name = "Timestamp", description = "")
public class FieldTyperTimestamp extends FieldTyperTimestampBase<FieldDescriptorTimestamp, FieldValueTimestamp, TwinFieldSearchTimestamp> {

    @FeaturerParam(name = "Pattern", description = "pattern for timestamp value", optional = true, defaultValue = "yyyy-MM-dd'T'HH:mm:ss")
    public static final FeaturerParamString pattern = new FeaturerParamString("pattern");

    @FeaturerParam(name = "HoursPast", description = "max hours in the past", optional = true, defaultValue = "-1")
    public static final FeaturerParamInt hoursPast = new FeaturerParamInt("hoursPast");

    @FeaturerParam(name = "HoursFuture", description = "max hours in the future", optional = true, defaultValue = "-1")
    public static final FeaturerParamInt hoursFuture = new FeaturerParamInt("hoursFuture");

    @Override
    public FieldDescriptorTimestamp getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        var now = LocalDateTime.now();
        var fieldDescriptorTimestamp = new FieldDescriptorTimestamp()
                .pattern(pattern.extract(properties))
                .beforeDate(now.minusHours(hoursPast.extract(properties)))
                .afterDate(now.plusHours(hoursFuture.extract(properties)));
        fieldDescriptorTimestamp.backendValidated(false);

        return fieldDescriptorTimestamp;
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldTimestampEntity twinFieldEntity, FieldValueTimestamp value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        Timestamp timestamp = null;

        if (!value.isNullified() && !StringUtils.isEmpty(value.getValue())) {
            var patternStr = pattern.extract(properties);

            // Validate format
            try {
                var formatter = DateTimeFormatter.ofPattern(patternStr);
                var parsedDateTime = LocalDateTime.parse(value.getValue(), formatter);
                timestamp = Timestamp.valueOf(parsedDateTime);
            } catch (DateTimeParseException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Timestamp value [" + value.getValue() + "] does not match pattern [" + patternStr + "]");
            }
        }

        detectValueChange(twinFieldEntity, twinChangesCollector, timestamp);
    }

    @Override
    protected FieldValueTimestamp deserializeValue(Properties properties, TwinField twinField, TwinFieldTimestampEntity twinFieldTimestampEntity) throws ServiceException {
        var fieldValueTimestamp = new FieldValueTimestamp(twinField.getTwinClassField());

        if (twinFieldTimestampEntity != null && twinFieldTimestampEntity.getValue() != null) {
            var patternStr = pattern.extract(properties);
            var formatter = DateTimeFormatter.ofPattern(patternStr);
            var localDateTime = twinFieldTimestampEntity.getValue().toLocalDateTime();
            fieldValueTimestamp.setValue(formatter.format(localDateTime));
        }

        return fieldValueTimestamp;
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchTimestamp search) {
        return TwinSpecification.checkFieldTimestamp(search);
    }

    @Override
    public ValidationResult validate(Properties properties, TwinEntity twin, FieldValueTimestamp value) {
        var result = new ValidationResult(true);

        try {
            var timestampValue = value.getValue();

            if (StringUtils.isEmpty(timestampValue) && !value.getTwinClassField().getRequired()) {
                return result;
            }

            if (StringUtils.isEmpty(timestampValue)) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
            }

            var patternStr = pattern.extract(properties);
            LocalDateTime localDateTime;

            try {
                var formatter = DateTimeFormatter.ofPattern(patternStr);
                localDateTime = LocalDateTime.parse(timestampValue, formatter);
            } catch (DateTimeParseException e) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Timestamp value [" + timestampValue + "] does not match pattern [" + patternStr + "]: " + e.getMessage());
            }

            var now = LocalDateTime.now();
            var minHours = hoursPast.extract(properties);
            if (minHours != null && minHours >= 0) {
                var minDate = now.minusHours(minHours);

                if (localDateTime.isBefore(minDate)) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Timestamp value [" + localDateTime + "] is more than " + minHours + " hours in the past");
                }
            }

            var maxHours = hoursFuture.extract(properties);
            if (maxHours != null && maxHours >= 0) {
                var maxDate = now.plusHours(maxHours);

                if (localDateTime.isAfter(maxDate)) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Timestamp value [" + localDateTime + "] is more than " + maxHours + " hours in the future");
                }
            }
        } catch (ServiceException e) {
            result = new ValidationResult(false, i18nService.translateToLocale(value.getTwinClassField().getBeValidationErrorI18nId()));
        }

        return result;
    }
}
