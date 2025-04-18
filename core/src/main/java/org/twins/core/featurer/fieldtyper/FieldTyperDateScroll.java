package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamInt;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.twins.core.dao.specifications.twin.TwinSpecification;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchDate;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Properties;

@Component
@Slf4j
@Featurer(id = FeaturerTwins.ID_1302,
        name = "Date",
        description = "")
public class FieldTyperDateScroll extends FieldTyperSimple<FieldDescriptorDate, FieldValueDate, TwinFieldSearchDate> {
    @FeaturerParam(name = "Pattern", description = "pattern for date value")
    public static final FeaturerParamString pattern = new FeaturerParamString("pattern");

    @FeaturerParam(name = "DaysPast", description = "number of days in the past", optional = true, defaultValue = "-1")
    public static final FeaturerParamInt daysPast = new FeaturerParamInt("daysPast");

    @FeaturerParam(name = "DaysFuture", description = "number of days in the futures", optional = true, defaultValue = "-1")
    public static final FeaturerParamInt daysFuture = new FeaturerParamInt("daysFuture");

    private static final String DATE_PATTERN = "yyyy-MM-dd";
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";

    @Override
    public FieldDescriptorDate getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorDate()
                .pattern(pattern.extract(properties))
                .daysPast(daysPast.extract(properties))
                .daysFuture(daysFuture.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueDate value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().getRequired() && StringUtils.isEmpty(value.getDate()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        Object dateValue = validateValue(twinFieldEntity, value.getDate(), properties);
        value.setDate(dateValue.toString());
        detectValueChange(twinFieldEntity, twinChangesCollector, value.getDate());
    }

    @Override
    protected FieldValueDate deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) {
        return new FieldValueDate(twinField.getTwinClassField())
                .setDate(twinFieldEntity != null && !StringUtils.isEmpty(twinFieldEntity.getValue()) ? validDateOrEmpty(twinFieldEntity.getValue(), properties) : null);
    }

    public String validDateOrEmpty(String dateStr, Properties properties) {
        if (GenericValidator.isDate(dateStr, pattern.extract(properties), false))
            return dateStr;
        else
            log.warn("Value[ " + dateStr + "] does not match expected format[" + pattern.extract(properties) + "]");
        return "";
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchDate search) {
        return Specification.where(TwinSpecification.checkFieldDate(search));
    }

    private Object parseDateTime(String value, Properties properties) throws ServiceException {
        String patternStr = pattern.extract(properties);
        try {
            if (DATE_PATTERN.equals(patternStr)) {
                return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } else if (DATE_TIME_PATTERN.equals(patternStr)) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                return LocalDateTime.parse(value, formatter);
            }
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    "Unsupported date pattern: " + patternStr);
        } catch (DateTimeParseException e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    "Value [" + value + "] is not a valid datetime in format " + patternStr);
        }
    }

    public Object validateValue(TwinFieldSimpleEntity twinFieldEntity, String value, Properties properties) throws ServiceException {
        String datePattern = pattern.extract(properties);
        boolean clearedValue = !twinFieldEntity.getTwinClassField().getRequired() && StringUtils.isEmpty(value);
        if (!GenericValidator.isDate(value, datePattern, false) && !clearedValue)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " date[" + value + "] does not match pattern[" + datePattern + "]");

        Object dateValue = parseDateTime(value, properties);
        LocalDate localDate = dateValue instanceof LocalDateTime ? ((LocalDateTime) dateValue).toLocalDate() : (LocalDate) dateValue;
        
        LocalDate now = LocalDate.now();
        Integer minDays = FieldTyperDateScroll.daysPast.extract(properties);
        if (minDays != null && minDays >= 0) {
            LocalDate minDate = now.minusDays(minDays);
            if (localDate.isBefore(minDate)) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                        "Date value [" + localDate + "] is more than " + minDays + " days in the past");
            }
        }
        Integer maxDays = FieldTyperDateScroll.daysFuture.extract(properties);
        if (maxDays != null && maxDays >= 0) {
            LocalDate maxDate = now.plusDays(maxDays);
            if (localDate.isAfter(maxDate)) {
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                        "Date value [" + localDate + "] is more than " + maxDays + " days in the future");
            }
        }
        return dateValue;
    }
}
