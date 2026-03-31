package org.twins.core.featurer.factory.conditioner;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;
import org.twins.core.service.factory.TwinFactoryService;

import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_24,
        name = "Conditioner",
        description = "")
@Slf4j
public abstract class Conditioner extends FeaturerTwins {
    @Lazy
    @Autowired
    TwinFactoryService factoryService;

    @Lazy
    @Autowired
    FieldLookupers fieldLookupers;

    public boolean check(TwinFactoryConditionEntity conditionEntity, FactoryItem factoryItem) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, conditionEntity.getConditionerParams());
        log.info("Checking conditioner[" + this.getClass().getSimpleName() + "] **" + conditionEntity.getDescription() + "** with params: " + properties.toString());
        return check(properties, factoryItem);
    }

    public abstract boolean check(Properties properties, FactoryItem factoryItem) throws ServiceException;
}
