package org.twins.core.featurer.fieldtyper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinChangesCollector;
import org.twins.core.domain.TwinField;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUrl;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = 1303,
        name = "FieldTyperUrl",
        description = "")
public class FieldTyperUrl extends FieldTyperBasic<FieldDescriptorUrl, FieldValueText> {
    @Override
    public FieldDescriptorUrl getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorUrl();
    }

    @Override
    protected void serializeValue(Properties properties, TwinFieldEntity twinFieldEntity, FieldValueText value, TwinChangesCollector twinChangesCollector) throws ServiceException {
        if (twinFieldEntity.getTwinClassField().isRequired() && StringUtils.isEmpty(value.getValue()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_REQUIRED, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " is required");
        if (new UrlValidator().isValid(value.getValue()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " incorrect url[" + value.getValue() + "]");
        detectValueChange(twinFieldEntity, twinChangesCollector, value.getValue());
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField, TwinFieldEntity twinFieldEntity) {
        return new FieldValueText(twinField.getTwinClassField(), twinFieldEntity != null)
                .setValue(twinFieldEntity != null && twinFieldEntity.getValue() != null ? twinFieldEntity.getValue() : "");
    }
}
