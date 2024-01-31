package org.twins.core.featurer.factory.multiplier;

import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamBoolean;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
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
@Featurer(id = 2207,
        name = "MultiplierIsolatedCopy",
        description = "New output twin for each input. Output class will be taken from input twin.")
public class MultiplierIsolatedCopy extends Multiplier {
    @FeaturerParam(name = "copyHead", description = "")
    public static final FeaturerParamBoolean copyHead = new FeaturerParamBoolean("copyHead");
    @Override
    public List<FactoryItem> multiply(Properties properties, List<TwinEntity> inputTwinList, FactoryContext factoryContext) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        List<FactoryItem> ret = new ArrayList<>();
        for (TwinEntity inputTwin : inputTwinList) {
            TwinEntity newTwin = new TwinEntity()
                    .setName("")
                    .setTwinClass(inputTwin.getTwinClass())
                    .setTwinClassId(inputTwin.getTwinClassId())
                    .setCreatedAt(Timestamp.from(Instant.now()))
                    .setCreatedByUserId(apiUser.getUser().getId())
                    .setCreatedByUser(apiUser.getUser());
            if (copyHead.extract(properties))
                newTwin
                        .setHeadTwin(inputTwin.getHeadTwin())
                        .setHeadTwinId(inputTwin.getHeadTwinId());
            TwinCreate twinCreate = new TwinCreate();
            twinCreate.setTwinEntity(newTwin);
            ret.add(new FactoryItem()
                    .setOutputTwin(twinCreate)
                    .setContextTwinList(List.of(inputTwin)));
        }
        return ret;
    }
}