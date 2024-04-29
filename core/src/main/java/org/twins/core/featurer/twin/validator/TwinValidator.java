package org.twins.core.featurer.twin.validator;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.Featurer;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;

import java.util.*;


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

    public CollectionValidationResult isValid(HashMap<String, String> validatorParams, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, validatorParams, new HashMap<>());
        log.info("Running " + (invert ? "inverted " : "") + " validator[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());
        return isValid(properties, twinEntityCollection, invert);
    }

    /**
     * Must be overridden to reduce db query count
     */
    protected CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        CollectionValidationResult collectionValidationResult = new CollectionValidationResult();
        ValidationResult singleTwinvalidationResult;
        for (TwinEntity twinEntity : twinEntityCollection) { // we will validate in loop, and that can produce N+1 query to DB
            singleTwinvalidationResult = isValid(properties, twinEntity, invert);
            collectionValidationResult.getTwinsResults().put(twinEntity.getId(), singleTwinvalidationResult);
        }
        return collectionValidationResult;
    }

    @Data
    @Accessors(chain = true)
    public static class ValidationResult {
        boolean valid = false;
        String message;
    }

    @Data
    @Accessors(chain = true)
    public static class CollectionValidationResult {
        Map<UUID, ValidationResult> twinsResults = new HashMap<>();
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
