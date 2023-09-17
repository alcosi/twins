package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorText;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = 1301,
        name = "FieldTyperTextField",
        description = "")
public class FieldTyperTextField extends FieldTyper<FieldDescriptorText, FieldValueText> {
    @FeaturerParam(name = "regexp", description = "")
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");

    @Override
    public FieldDescriptorText getFieldDescriptor(Properties properties) {
        return new FieldDescriptorText()
                .regExp(regexp.extract(properties));
    }

    @Override
    protected String serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueText value) throws ServiceException {
        if (twinFieldEntity.twinClassField().isRequired() && StringUtils.isEmpty(value.value()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.twinClassField().logShort() + " is required");
        String pattern = regexp.extract(properties);
        if (!value.value().matches(pattern))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.twinClassField().logShort() + " value[" + value.value() + "] does not match pattern[" + pattern + "]");
        return value.value();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinFieldEntity twinFieldEntity, Object value) {
        return new FieldValueText().value(value != null ? value.toString() : "");
    }
}
