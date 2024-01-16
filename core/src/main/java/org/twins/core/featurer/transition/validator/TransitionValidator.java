package org.twins.core.featurer.transition.validator;

import lombok.Data;
import lombok.experimental.Accessors;
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

    public ValidationResult isValid(HashMap<String, String> validatorParams, TwinEntity twinEntity) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, validatorParams, new HashMap<>());
        log.info("Running validator[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        return isValid(properties, twinEntity);
    }

    protected abstract ValidationResult isValid(Properties properties, TwinEntity twinEntity) throws ServiceException;

    @Data
    @Accessors(chain = true)
    public static class ValidationResult {
        boolean valid = false;
        String message;
    }
}
