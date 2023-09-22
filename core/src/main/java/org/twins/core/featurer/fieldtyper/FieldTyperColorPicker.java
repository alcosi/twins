package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorColorPicker;
import org.twins.core.featurer.fieldtyper.value.FieldValueColorHEX;

import java.util.Properties;

@Component
@Featurer(id = 1304,
        name = "FieldTyperColorPicker",
        description = "")
public class FieldTyperColorPicker extends FieldTyper<FieldDescriptorColorPicker, FieldValueColorHEX> {
    private static final String HEX_PATTERN
            = "^#([a-fA-F0-9]{6})$";
    @Override
    public FieldDescriptorColorPicker getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorColorPicker();
    }

    @Override
    protected String serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueColorHEX value) throws ServiceException {
        if (twinFieldEntity.twinClassField().isRequired() && StringUtils.isEmpty(value.hex()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED,  twinFieldEntity.twinClassField().logShort() + " is required");
        if (!value.hex().matches(HEX_PATTERN))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.twinClassField().logShort() +  " hex[" + value.hex() + "] does not match pattern[" + HEX_PATTERN + "]");
        return value.hex();
    }

    @Override
    protected FieldValueColorHEX deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity, Object value) {
        return new FieldValueColorHEX().hex(value != null ? value.toString() : "");
    }
}
