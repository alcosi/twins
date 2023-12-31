package org.twins.core.featurer.transition.validator;

import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;

import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = 16,
        name = "TransitionValidator",
        description = "")
@Slf4j
public abstract class TransitionValidator extends Featurer {

    public boolean isValid(HashMap<String, String> validatorParams, TwinEntity twinEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, validatorParams, new HashMap<>());
        log.info("Running trigger[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        return isValid(properties, twinEntity);
    }

    protected abstract boolean isValid(Properties properties, TwinEntity twinEntity) throws ServiceException;
}
