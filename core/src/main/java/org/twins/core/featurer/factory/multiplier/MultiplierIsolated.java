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
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2202,
        name = "Isolated",
        description = "New output twin for each input. Output class from params")
public class MultiplierIsolated extends Multiplier {
    @FeaturerParam(name = "Output twin class id", description = "", order = 1)
    public static final FeaturerParamUUID outputTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("outputTwinClassId");

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
