package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = 24,
        name = "Conditioner",
        description = "")
@Slf4j
public abstract class Conditioner extends Featurer {
    @Lazy
    @Autowired
    TwinFactoryService factoryService;

    public boolean check(TwinFactoryConditionEntity conditionEntity, FactoryItem factoryItem) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, conditionEntity.getConditionerParams(), new HashMap<>());
        log.info("Checking conditioner[" + this.getClass().getSimpleName() + "] **" + conditionEntity.getDescription() + "** with params: " + properties.toString());
        return check(properties, factoryItem);
    }

    public abstract boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException;
}
