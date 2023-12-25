package org.twins.core.featurer.factory.filler;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.factory.FactoryItem;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = 23,
        name = "Filler",
        description = "")
@Slf4j
public abstract class Filler extends Featurer {

    public void fill(HashMap<String, String> fillerParams, FactoryItem factoryItem, TwinEntity templateTwin, String logMsg) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, fillerParams, new HashMap<>());
        log.info(logMsg + ": running filler[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        fill(properties, factoryItem, templateTwin);
    }

    public abstract void fill(Properties properties, FactoryItem factoryItem, TwinEntity templateTwin) throws ServiceException;

    public boolean canBeOptional() {
        return true; // most steps can be option by default. otherwise method must be overridden
    }

    public static TwinEntity checkNotMultiplyContextTwin(FactoryItem factoryItem) throws ServiceException {
        if (factoryItem.getContextTwinList().size() == 0)
            return null;
        else if (factoryItem.getContextTwinList().size() > 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "context twin size > 1. Please check multiplier");
        else
            return factoryItem.getContextTwinList().get(0);
    }

    public static TwinEntity checkSingleContextTwin(FactoryItem factoryItem) throws ServiceException {
        if (factoryItem.getContextTwinList().size() != 1)
            throw new ServiceException(ErrorCodeTwins.FACTORY_PIPELINE_STEP_ERROR, "context twin size != 1. Please check multiplier");
        else
            return factoryItem.getContextTwinList().get(0);
    }
}
