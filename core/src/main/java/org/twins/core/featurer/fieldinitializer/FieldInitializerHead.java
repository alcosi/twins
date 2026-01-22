package org.twins.core.featurer.fieldinitializer;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.fieldtyper.descriptor.FieldDescriptor;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassFieldId;
import org.twins.core.service.twin.TwinService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_5308,
        name = "Value initializer from head",
        description = "")
@Slf4j
public class FieldInitializerHead<D extends FieldDescriptor, T extends FieldValue> extends FieldInitializer<D, T> implements FieldInitializerThrowIfNull{
    @FeaturerParam(name = "Head field id", description = "", optional = false, order = 1)
    public static final FeaturerParamUUID fromTwinClassFieldId = new FeaturerParamUUIDTwinsTwinClassFieldId("fromTwinClassFieldId");

    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    protected void setInitValue(Properties properties, TwinEntity twin, T value) throws ServiceException {
        if (twin.getHeadTwinId() == null) {
            throw new ServiceException(ErrorCodeTwins.CONFIGURATION_IS_INVALID, twin.logNormal() + " has no head twin");
        }
        twinService.loadHeadForTwin(twin);
        twinService.loadTwinFields(twin.getHeadTwin());
        var headTwinValue = twin.getHeadTwin().getFieldValuesKit().get(fromTwinClassFieldId.extract(properties));
        if (headTwinValue != null && !headTwinValue.isEmpty()) {
            twinService.copyToField(headTwinValue, value);
        } else if (throwIfNull.extract(properties)) {
            throw new ServiceException(ErrorCodeTwins.CONFIGURATION_IS_INVALID, twin.logNormal() + " has no value in head twin field " + fromTwinClassFieldId.extract(properties));
        }
    }
}
