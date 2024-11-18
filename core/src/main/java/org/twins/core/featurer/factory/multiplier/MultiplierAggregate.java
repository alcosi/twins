package org.twins.core.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2201,
        name = "MultiplierAggregate",
        description = "Only one output twin, even for multiple input.  Output class from params")
public class MultiplierAggregate extends Multiplier {

    @FeaturerParam(name = "outputTwinClassId", description = "")
    public static final FeaturerParamUUID outputTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("outputTwinClassId");
    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        TwinClassEntity outputTwinClassEntity = twinClassService.findEntitySafe(outputTwinClassId.extract(properties));
        ApiUser apiUser = authService.getApiUser();
        TwinEntity newTwin = new TwinEntity()
                .setName("")
                .setTwinClass(outputTwinClassEntity)
                .setTwinClassId(outputTwinClassEntity.getId())
                .setCreatedAt(Timestamp.from(Instant.now()))
                .setCreatedByUserId(apiUser.getUser().getId())
                .setCreatedByUser(apiUser.getUser());
        TwinCreate twinCreate = new TwinCreate();
        twinCreate.setTwinEntity(newTwin);
        return List.of(
                new FactoryItem()
                        .setOutput(twinCreate)
                        .setContextFactoryItemList(inputFactoryItemList));
    }
}
