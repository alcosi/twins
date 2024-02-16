package org.twins.core.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinCreate;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = 2202,
        name = "MultiplierIsolated",
        description = "New output twin for each input. Output class from params")
public class MultiplierIsolated extends Multiplier {
    @FeaturerParam(name = "outputTwinClassId", description = "")
    public static final FeaturerParamUUID outputTwinClassId = new FeaturerParamUUID("outputTwinClassId");

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        TwinClassEntity outputTwinClassEntity = twinClassService.findEntitySafe(outputTwinClassId.extract(properties));
        ApiUser apiUser = authService.getApiUser();
        List<FactoryItem> ret = new ArrayList<>();
        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity newTwin = new TwinEntity()
                    .setName("")
                    .setTwinClass(outputTwinClassEntity)
                    .setTwinClassId(outputTwinClassEntity.getId())
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUser().getId())
                    .setCreatedByUser(apiUser.getUser());
            TwinCreate twinCreate = new TwinCreate();
            twinCreate.setTwinEntity(newTwin);
            ret.add(new FactoryItem()
                    .setOutput(twinCreate)
                    .setContextFactoryItemList(List.of(inputItem)));
        }
        return ret;
    }
}
