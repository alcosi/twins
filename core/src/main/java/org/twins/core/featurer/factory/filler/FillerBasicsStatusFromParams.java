package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinFieldEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.fieldtyper.value.FieldValue;
import org.twins.core.mappers.rest.twin.TwinFieldRestDTOMapperV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twin.TwinStatusService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Properties;

@Component
@Featurer(id = 2313,
        name = "FillerBasicsStatusFromParams",
        description = "")
@Slf4j
public class FillerBasicsStatusFromParams extends Filler {
    @FeaturerParam(name = "newTwinStatusId", description = "")
    public static final FeaturerParamUUID newTwinStatusId = new FeaturerParamUUID("newTwinStatusId");

    @Lazy
    @Autowired
    TwinStatusService twinStatusService;

    @Override
    public void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException {
        TwinEntity outputTwinEntity = factoryItem.getOutputTwin().getTwinEntity();
        TwinStatusEntity twinStatusEntity = twinStatusService.findEntitySafe(newTwinStatusId.extract(properties));
        if (!twinStatusService.checkStatusAllowed(outputTwinEntity, twinStatusEntity))
            throw new ServiceException(ErrorCodeTwins.TWIN_STATUS_INCORRECT, twinStatusEntity.logShort() + " is not allowed for " + outputTwinEntity.getTwinClass().logNormal());
        outputTwinEntity
                .setTwinStatus(twinStatusEntity)
                .setTwinStatusId(twinStatusEntity.getId());
    }
}
