package org.twins.core.featurer.fieldtyper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.EntitiesChangesCollector;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorDate;
import org.twins.core.featurer.fieldtyper.value.FieldValueDate;

import java.util.Properties;

@Component
@Slf4j
@Featurer(id = 1302,
        name = "FieldTyperDateScroll",
        description = "")
public class FieldTyperDateScroll extends FieldTyper<FieldDescriptorDate, FieldValueDate> {
    @FeaturerParam(name = "pattern", description = "")
    public static final FeaturerParamString pattern = new FeaturerParamString("pattern");

    @Override
    public FieldDescriptorDate getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorDate()
                .pattern(pattern.extract(properties));
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueDate value, EntitiesChangesCollector entitiesChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().isRequired() && StringUtils.isEmpty(value.getDate()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        String datePatter = pattern.extract(properties);
        if (!GenericValidator.isDate(value.getDate(), datePatter, false))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " date[" + value.getDate() + "] does not match pattern[" + datePatter + "]");
        detectLocalChange(twinFieldEntity, entitiesChangesCollector, value.getDate());
    }

    @Override
    protected FieldValueDate deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity) {
        return new FieldValueDate().setDate(twinFieldEntity.getValue() != null ? validDateOrEmpty(twinFieldEntity.getValue(), properties) : "");
    }

    public String validDateOrEmpty(String dateStr, Properties properties) {
        if (GenericValidator.isDate(dateStr, pattern.extract(properties), false))
            return dateStr;
        else
            log.warn("Value[ " + dateStr + "] does not match expected format[" + pattern.extract(properties) + "]");
        return "";
    }
}
