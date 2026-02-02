package org.twins.core.featurer.factory.multiplier;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.domain.factory.FactoryContext;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.List;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_22,
        name = "Multiplier",
        description = "")
@Slf4j
public abstract class Multiplier extends FeaturerTwins {
    @Lazy
    @Autowired
    TwinClassService twinClassService;

    @Lazy
    @Autowired
    AuthService authService;

    public List<FactoryItem> multiply(TwinFactoryMultiplierEntity multiplierEntity, List<FactoryItem> input, FactoryContext factoryContext) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, multiplierEntity.getMultiplierParams());
//        log.info("Running multiplier[" + this.getClass().getSimpleName() + "] **" + multiplierEntity.getComment() + "** with params: " + properties.toString());
        List<FactoryItem> ret =  multiply(properties, input, factoryContext);
        for (FactoryItem factoryItem : ret) {
            factoryItem.setFactoryContext(factoryContext);
        }
        return ret;
    }

    public abstract List<FactoryItem> multiply(Properties properties, List<FactoryItem> inputFactoryItemList, FactoryContext factoryContext) throws ServiceException;

//    public List<TwinEntity> extractEntityList(List<FactoryItem> inputFactoryItemList) {
//        List<TwinEntity> ret = new ArrayList<>();
//        for (FactoryItem factoryItem : inputFactoryItemList) {
//            TwinOperation twinOperation = factoryItem.getOutputTwin();
//            if (twinOperation instanceof TwinUpdate twinUpdate)
//                ret.add(twinUpdate.getDbTwinEntity());
//            else
//                ret.add(twinOperation.getTwinEntity());
//        }
//        return ret;
//    }
}
