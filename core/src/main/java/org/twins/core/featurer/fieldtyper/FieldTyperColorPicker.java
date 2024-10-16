package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorColorPicker;
import org.twins.core.featurer.fieldtyper.value.FieldValueColorHEX;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1304,
        name = "FieldTyperColorPicker",
        description = "")
public class FieldTyperColorPicker extends FieldTyperSimple<FieldDescriptorColorPicker, FieldValueColorHEX, TwinFieldSearchNotImplemented> {
    private static final String HEX_PATTERN
            = "^#([a-fA-F0-9]{6})$";
    @Override
    public FieldDescriptorColorPicker getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorColorPicker();
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueColorHEX value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().getRequired() && StringUtils.isEmpty(value.getHex()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED,  twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        if (!value.getHex().matches(HEX_PATTERN))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) +  " hex[" + value.getHex() + "] does not match pattern[" + HEX_PATTERN + "]");
        detectValueChange(twinFieldEntity, twinChangesCollector, value.getHex());
    }

    @Override
    protected FieldValueColorHEX deserializeValue(Properties properties, TwinField twinField, TwinFieldSimpleEntity twinFieldEntity) {
        return new FieldValueColorHEX(twinField.getTwinClassField())
                .setHex(twinFieldEntity != null && twinFieldEntity.getValue() != null ? twinFieldEntity.getValue() : null);
    }
}
