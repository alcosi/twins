package org.twins.core.featurer.fieldtyper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Properties;

@Component
@Featurer(id = 1303,
        name = "FieldTyperUrl",
        description = "")
public class FieldTyperUrl extends FieldTyper<FieldValueText> {
       @Override
    public FieldTypeUIDescriptor getUiDescriptor(Properties properties) {
        return new FieldTypeUIDescriptor()
                .type("url");
    }

    @Override
    protected String serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueText value) throws ServiceException {
        if (twinFieldEntity.twinClassField().required() && StringUtils.isEmpty(value.value()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, "twinClassField[" + twinFieldEntity.twinClassFieldId() + "] is required");
        if (new UrlValidator().isValid(value.value()) )
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, "incorrect url[" + value.value() + "]");
        return value.value();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, Object value) {
        return new FieldValueText().value(value != null ? value.toString() : "");
    }
}
