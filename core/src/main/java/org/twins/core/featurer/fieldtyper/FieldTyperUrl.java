package org.twins.core.featurer.fieldtyper;

import org.cambium.common.EasyLoggable;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.UrlUtils;
import org.cambium.featurer.annotations.Featurer;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinFieldSimpleEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.TwinField;
import org.twins.core.domain.search.TwinFieldSearchNotImplemented;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptorUrl;
import org.twins.core.featurer.fieldtyper.value.FieldValueText;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_1303,
        name = "Url",
        description = "")
public class FieldTyperUrl extends FieldTyperSimple<FieldDescriptorUrl, FieldValueText, TwinFieldSearchNotImplemented> {
    @Override
    public FieldDescriptorUrl getFieldDescriptor(TwinClassFieldEntity twinClassFieldEntity, Properties properties) {
        return new FieldDescriptorUrl();
    }

    @Override
    protected String processValue(Properties properties, TwinFieldSimpleEntity twinFieldEntity, FieldValueText value) throws ServiceException {
        if (!UrlUtils.isValid(value.getValue()))
            throw new ServiceException(ErrorCodeTwins.TWIN_CLASS_FIELD_VALUE_INCORRECT, twinFieldEntity.getTwinClassField().easyLog(EasyLoggable.Level.NORMAL) + " incorrect url[" + value.getValue() + "]");
        return value.getValue();
    }

    @Override
    protected FieldValueText deserializeValue(Properties properties, TwinField twinField) throws ServiceException {
        var twinFieldEntity = resolveTwinFieldEntity(twinField.getTwin(), twinField.getTwinClassField());
        return new FieldValueText(twinField.getTwinClassField())
                .setValue(twinFieldEntity != null && twinFieldEntity.getValue() != null ? twinFieldEntity.getValue() : null);
    }
}
