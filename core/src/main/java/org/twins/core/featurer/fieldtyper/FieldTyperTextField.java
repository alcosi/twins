package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamString;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Properties;

@Component
@Featurer(id = 1301,
        name = "FieldTyperTextField",
        description = "")
public class FieldTyperTextField extends FieldTyper<FieldValueText> {
    @FeaturerParam(name = "regexp", description = "")
    public static final FeaturerParamString regexp = new FeaturerParamString("regexp");

    @Override
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        return new FieldTypeUIDescriptor()
                .type("textField")
                .addParam("regexp", regexp.extract(properties));
    }

    @Override
    protected String serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueText value) throws ServiceException {
        if (twinFieldEntity.twinClassField().required() && StringUtils.isEmpty(value.value()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, "twinClassField[" + twinFieldEntity.twinClassFieldId() + "] is required");
        String pattern = regexp.extract(properties);
        if (!value.value().matches(pattern))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "text[" + value.value() + "] for twinClassField[" + twinFieldEntity.twinClassFieldId() + "] does not match pattern[" + pattern + "]");
        return value.value();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, Object value) {
        return new FieldValueText().value(value != null ? value.toString() : "");
    }
}
