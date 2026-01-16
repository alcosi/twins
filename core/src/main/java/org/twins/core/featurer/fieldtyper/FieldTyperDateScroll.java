package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.cambium.common.EasyLoggable;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UuidUtils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;

@Component
@Slf4j
@Featurer(id = FeaturerTwins.ID_1302, name = "Date", description = "")
public class FieldTyperDateScroll extends FieldTyperSimple<FieldDescriptorDate, FieldValueDate, TwinFieldSearchDate> {
    @FeaturerParam(name = "Pattern", description = "pattern for date value")
    public static final FeaturerParamString pattern = new FeaturerParamString("pattern");
    @FeaturerParam(name = "HoursPast", description = "number of hours in the past", optional = true, defaultValue = "-1")
    public static final FeaturerParamInt hoursPast = new FeaturerParamInt("hoursPast");
    @FeaturerParam(name = "HoursFuture", description = "number of hours in the futures", optional = true, defaultValue = "-1")
    public static final FeaturerParamInt hoursFuture = new FeaturerParamInt("hoursFuture");

    @Override
    public FieldDescriptorDate getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        LocalDateTime now = LocalDateTime.now();
        FieldDescriptorDate fieldDescriptorDate = new FieldDescriptorDate()
                .pattern(pattern.extract(properties))
                .beforeDate(now.minusHours(hoursPast.extract(properties)))
                .afterDate(now.plusHours(hoursFuture.extract(properties)));
        fieldDescriptorDate.backendValidated(false);
        return fieldDescriptorDate;
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueDate value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (!value.isNullified()) {
            LocalDateTime localDateTime = parseDateTime(value.getDateStr(), properties);
            value.setDate(localDateTime);
            value.setDateStr(formatDate(localDateTime, properties));
        }
        detectValueChange(twinFieldEntity, twinChangesCollector, value.getDateStr());
    }

    @Override
    protected FieldValueDate deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) throws ServiceException {
        FieldValueDate fieldValueDate = new FieldValueDate(twinField.getTwinClassField());
        fieldValueDate
                .setDateStr(twinFieldEntity != null && !StringUtils.isEmpty(twinFieldEntity.getValue()) ? validDateOrEmpty(twinFieldEntity.getValue(), properties) : null)
                .setDate(fieldValueDate.getDateStr() != null
                        ? parseDateTime(fieldValueDate.getDateStr(), properties)
                        : null);
        return fieldValueDate;
    }

    public String validDateOrEmpty(String dateStr, Properties properties) {
        if (GenericValidator.isDate(dateStr, pattern.extract(properties), false)) {
            return dateStr;
        }
        log.warn("Value[{}] does not match expected format[{}]", dateStr, pattern.extract(properties));
        return "";
    }

    @Override
    public Specification<TwinEntity> searchBy(TwinFieldSearchDate search) {
        return TwinSpecification.checkFieldDate(search);
    }

    private LocalDateTime parseDateTime(String value, Properties properties) throws ServiceException {
        String patternStr = pattern.extract(properties);
        SimpleDateFormat formatter = new SimpleDateFormat(patternStr);
        formatter.setLenient(false);
        try {
            return formatter.parse(value).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (ParseException var6) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Value [" + value + "] is not a valid datetime in format " + patternStr);
        }
    }

    private String formatDate(LocalDateTime dateTime, Properties properties) {
        String patternStr = pattern.extract(properties);
        SimpleDateFormat formatter = new SimpleDateFormat(patternStr);
        formatter.setLenient(false);
        Date date = Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
        return formatter.format(date);
    }

    @Override
    public ValidationResult validate(Properties properties, TwinEntity twin, FieldValueDate value) throws ServiceException {
        String datePattern = pattern.extract(properties);
        ValidationResult result = new ValidationResult(true);
        if (value.isNullifyValue())
            return result;
        try {
            String dateValue = value.getDateStr();
            if (StringUtils.isEmpty(dateValue) && !value.getTwinClassField().getRequired())
                return result;
            if (!GenericValidator.isDate(dateValue, datePattern, false))
                throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, value.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " date[" + value + "] does not match pattern[" + datePattern + "]");
            LocalDateTime localDateTime = parseDateTime(dateValue, properties);
            LocalDateTime now = LocalDateTime.now();
            Integer minHours = FieldTyperDateScroll.hoursPast.extract(properties);
            if (minHours != null && minHours >= 0) {
                LocalDateTime minDate = now.minusHours(minHours);
                if (localDateTime.isBefore(minDate)) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Date value [" + localDateTime + "] is more than " + minHours + " hours in the past");
                }
            }
            Integer maxHours = FieldTyperDateScroll.hoursFuture.extract(properties);
            if (maxHours != null && maxHours >= 0) {
                LocalDateTime maxDate = now.plusHours(maxHours);
                if (localDateTime.isAfter(maxDate)) {
                    throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "Date value [" + localDateTime + "] is more than " + maxHours + " hours in the future");
                }
            }
        } catch (ServiceException e) {
            result = new ValidationResult(false, i18nService.translateToLocale(value.getTwinClassField().getBeValidationErrorI18nId()));
        }
        return result;
    }

}