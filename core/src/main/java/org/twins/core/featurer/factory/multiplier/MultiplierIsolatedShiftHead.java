package org.twins.core.featurer.factory.multiplier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassEntity;
import org.twins.core.domain.ApiUser;
import org.twins.core.domain.TwinCreate;
import org.twins.core.domain.TwinUpdate;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.service.twin.TwinService;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Slf4j
@Component
@Featurer(id = 2204,
        name = "MultiplierIsolatedShiftHead",
        description = "Output twin for each input. Output twin will be loaded from head")
public class MultiplierIsolatedShiftHead extends Multiplier {
    @Lazy
    @Autowired
    TwinService twinService;

    @Override
    public List<FactoryItem> multiply(Properties properties, List<TwinEntity> inputTwinList, FactoryContext factoryContext) throws ServiceException {
        List<FactoryItem> ret = new ArrayList<>();
        for (TwinEntity inputTwin : inputTwinList) {
            twinService.loadHeadForTwin(inputTwin);
            if (inputTwin.getHeadTwin() == null) {
                log.error(inputTwin.logShort() + " no head twin. Skipped by multiplier");
                continue;
            }

            TwinUpdate twinUpdate = new TwinUpdate();
            twinUpdate.setTwinEntity(inputTwin.getHeadTwin());
            ret.add(new FactoryItem()
                    .setOutputTwin(twinUpdate)
                    .setContextTwinList(List.of(inputTwin)));
        }
        return ret;
    }
}
