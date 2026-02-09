package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.featurer.FeaturerTwins;
import org.twins.core.featurer.factory.lookuper.FieldLookupers;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_23,
        name = "Filler",
        description = "")
@Slf4j
public abstract class Filler extends FeaturerTwins {
    @Lazy
    @Autowired
    FieldLookupers fieldLookupers;

    public void fill(HashMap<String, String> fillerParams, FactoryItem factoryItem, TwinEntity templateTwin, String logMsg) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fillerParams);
        log.info(logMsg + ": running filler[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        fill(properties, factoryItem, templateTwin);
    }

    public abstract void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException;

    public boolean canBeOptional() {
        return true; // most steps can be option by default. otherwise method must be overridden
    }


}
