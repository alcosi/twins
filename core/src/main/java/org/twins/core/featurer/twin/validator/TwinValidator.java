package org.twins.core.featurer.twin.validator;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Properties;


@FeaturerType(id = 16,
        name = "TwinValidator",
        description = "")
@Slf4j
public abstract class TwinValidator extends Featurer {

    public ValidationResult isValid(HashMap<String, String> validatorParams, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, validatorParams, new HashMap<>());
        log.info("Running " + (invert ? "inverted " : "") + " validator[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        return isValid(properties, twinEntity, invert);
    }

    protected abstract ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException;

    public void beforeListValidation(Collection<TwinEntity> twinEntities) {

    }

    @Data
    @Accessors(chain = true)
    public static class ValidationResult {
        boolean inverted = false;
        boolean valid = false;
        String message;
    }

    protected ValidationResult buildResult(boolean isValid, boolean invert, String invalidMessage, String invertedInvalidMessage) {
        if (invert) {
            isValid = !isValid;
            return new ValidationResult()
                    .setValid(isValid)
                    .setMessage(isValid ? "" : invertedInvalidMessage);
        }
        return new ValidationResult()
                .setValid(isValid)
                .setMessage(isValid ? "" : invalidMessage);

    }
}
