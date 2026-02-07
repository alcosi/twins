package org.twins.core.featurer.transition.trigger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_15,
        name = "TransitionTrigger",
        description = "")
@Slf4j
public abstract class TwinTrigger extends FeaturerTwins {

    public void run(HashMap<String, String> triggerParams, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, triggerParams);
        log.info("Running trigger[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        run(properties, twinEntity, srcTwinStatus, dstTwinStatus);
    }

    public abstract void run(Properties properties , TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException ;
}
