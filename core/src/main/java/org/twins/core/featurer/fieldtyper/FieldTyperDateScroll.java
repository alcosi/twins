package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamDateTime;
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
    public static final Integer ID = 1302;

    @FeaturerParam(name = "Pattern", description = "pattern for date value")
    public static final FeaturerParamString pattern = new FeaturerParamString("pattern");
    @FeaturerParam(name = "From date", description = "pattern for date value")
    public static final FeaturerParamDateTime fromDate = new FeaturerParamDateTime("fromDate");
    @FeaturerParam(name = "To date", description = "pattern for date value")
    public static final FeaturerParamDateTime toDate = new FeaturerParamDateTime("toDate");

    @Override
    public FieldDescriptorDate getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorDate()
                .pattern(pattern.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueDate value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().getRequired() && StringUtils.isEmpty(value.getDate()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        String datePatter = pattern.extract(properties);
        boolean clearedValue = !twinFieldEntity.getTwinClassField().getRequired() && StringUtils.isEmpty(value.getDate());
        if (!GenericValidator.isDate(value.getDate(), datePatter, false) && !clearedValue)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " date[" + value.getDate() + "] does not match pattern[" + datePatter + "]");
        validateValue(value.getDate(), properties);
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
    public Specification<TwinEntity> searchBy(TwinFieldSearchDate search) throws ServiceException {
        return Specification.where(TwinSpecification.checkFieldDate(search));
    }

    public void validateValue(String value, Properties params) throws ServiceException {
        LocalDateTime dateValue = parseDateTime(value);

        LocalDateTime fromDate = FieldTyperDateScroll.fromDate.extract(params);
        if (fromDate != null && dateValue.isBefore(fromDate)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    "Date value [" + dateValue + "] is before allowed minimum date [" + fromDate + "]");
        }

        LocalDateTime toDate = FieldTyperDateScroll.toDate.extract(params);
        if (toDate != null && dateValue.isAfter(toDate)) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    "Date value [" + dateValue + "] is after allowed maximum date [" + toDate + "]");
        }
    }

    private LocalDateTime parseDateTime(String value) throws ServiceException {
        try {
            return LocalDateTime.parse(value, DateTimeFormatter.ofPattern(FeaturerParamDateTime.DATETIME_PATTERN));
        } catch (DateTimeParseException e) {
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT,
                    "Value [" + value + "] is not a valid datetime in format " + FeaturerParamDateTime.DATETIME_PATTERN);
        }
    }
}
