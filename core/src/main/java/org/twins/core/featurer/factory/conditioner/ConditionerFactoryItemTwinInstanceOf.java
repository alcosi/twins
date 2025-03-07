package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.Featurer;
import org.cambium.featurer.annotations.FeaturerParam;
import org.cambium.featurer.params.FeaturerParamUUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.params.FeaturerParamUUIDTwinsTwinClassId;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Properties;

@Component
@Featurer(id = FeaturerTwins.ID_2413,
        name = "Factory item twin instance of",
        description = "")
@Slf4j
public class ConditionerFactoryItemTwinInstanceOf extends Conditioner {
    @FeaturerParam(name = "Instance of twin class id", description = "", order = 1)
    public static final FeaturerParamUUID instanceOfTwinClassId = new FeaturerParamUUIDTwinsTwinClassId("instanceOfTwinClassId");

    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Override
    public boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException {
        return twinClassService.isInstanceOf(factoryItem.getOutput().getTwinEntity().getTwinClass(), instanceOfTwinClassId.extract(properties));
    }
}
