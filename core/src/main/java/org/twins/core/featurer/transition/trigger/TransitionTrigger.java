package org.twins.core.featurer.transition.trigger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twin.TwinStatusEntity;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = 15,
        name = "TransitionTrigger",
        description = "")
@Slf4j
public abstract class TransitionTrigger extends Featurer {

    public void run(HashMap<String, String> triggerParams, TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, triggerParams, new HashMap<>());
        log.info("Running trigger[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        run(properties, twinEntity, srcTwinStatus, dstTwinStatus);
    }

    public abstract void run(Properties properties , TwinEntity twinEntity, TwinStatusEntity srcTwinStatus, TwinStatusEntity dstTwinStatus) throws ServiceException ;
}
