package org.twins.core.featurer.motion.trigger;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = FeaturerTwins.TYPE_32,
        name = "MotionTrigger",
        description = "")
@Slf4j
public abstract class MotionTrigger extends FeaturerTwins {

    public void run(HashMap<String, String> triggerParams, TwinEntity twinEntity, TwinClassFieldEntity twinClassField) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, triggerParams, new HashMap<>());
        log.info("Running trigger[{}] with params: {}", this.getClass().getSimpleName(), properties.toString());
        run(properties, twinEntity, twinClassField);
    }

    public abstract void run(Properties properties, TwinEntity twinEntity, TwinClassFieldEntity twinClassField) throws ServiceException ;
}
