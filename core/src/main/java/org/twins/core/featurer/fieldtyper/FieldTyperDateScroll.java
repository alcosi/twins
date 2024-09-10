package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.util.Properties;

@Component
@Slf4j
@Featurer(id = FeaturerTwins.ID_1302,
        name = "FieldTyperDateScroll",
        description = "")
public class FieldTyperDateScroll extends FieldTyperSimple<FieldDescriptorDate, FieldValueDate> {
    @FeaturerParam(name = "pattern", description = "pattern for date value")
    public static final FeaturerParamString pattern = new FeaturerParamString("pattern");

    @Override
    public FieldDescriptorDate getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorDate()
                .pattern(pattern.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueDate value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().isRequired() && StringUtils.isEmpty(value.getDate()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        String datePatter = pattern.extract(properties);
        boolean clearedValue = !twinFieldEntity.getTwinClassField().isRequired() && StringUtils.isEmpty(value.getDate());
        if (!GenericValidator.isDate(value.getDate(), datePatter, false) && !clearedValue)
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " date[" + value.getDate() + "] does not match pattern[" + datePatter + "]");
        detectValueChange(twinFieldEntity, twinChangesCollector, value.getDate());

    }

    @Override
    protected FieldValueDate deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) {
        return new FieldValueDate(twinField.getTwinClassField())
                .setDate(twinFieldEntity != null && twinFieldEntity.getValue() != null ? validDateOrEmpty(twinFieldEntity.getValue(), properties) : null);
    }

    public String validDateOrEmpty(String dateStr, Properties properties) {
        if (GenericValidator.isDate(dateStr, pattern.extract(properties), false))
            return dateStr;
        else
            log.warn("Value[ " + dateStr + "] does not match expected format[" + pattern.extract(properties) + "]");
        return "";
    }
}
