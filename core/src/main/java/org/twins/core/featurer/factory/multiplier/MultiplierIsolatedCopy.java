package org.twins.core.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.domain.twinoperation.TwinCreate;
import org.twins.core.featurer.FeaturerTwins;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Component
@Featurer(
        id = FeaturerTwins.ID_2207,
        name = "Isolated copy",
        description = "New output twin for each input. Output class will be taken from input twin."
)
public class MultiplierIsolatedCopy extends Multiplier {

    @FeaturerParam(name = "Copy head", description = "", order = 1)
    public static final FeaturerParamBoolean copyHead = new FeaturerParamBoolean("copyHead");

    @Override
    public List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<FactoryItem> ret = new ArrayList<>();

        for (FactoryItem inputItem : inputFactoryItemList) {
            TwinEntity newTwin = new TwinEntity()
                    .setName("")
                    .setTwinClass(inputItem.getTwin().getTwinClass())
                    .setTwinClassId(inputItem.getTwin().getTwinClassId())
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUser().getId())
                    .setCreatedByUser(apiUser.getUser());

            if (copyHead.extract(properties)) {
                newTwin
                        .setHeadTwin(inputItem.getTwin().getHeadTwin())
                        .setHeadTwinId(inputItem.getTwin().getHeadTwinId());
            }

            TwinCreate twinCreate = new TwinCreate();
            twinCreate.setTwinEntity(newTwin);
            ret.add(
                    new FactoryItem()
                            .setOutput(twinCreate)
                            .setContextFactoryItemList(List.of(inputItem))
            );
        }
        return ret;
    }
}