package org.twins.core.featurer.twin.validator;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.CollectionUtils;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;


@FeaturerType(id = FeaturerTwins.TYPE_16,
        name = "TwinValidator",
        description = "")
@Slf4j
public abstract class TwinValidator extends FeaturerTwins {

    public ValidationResult isValid(HashMap<String, String> validatorParams, TwinEntity twinEntity, boolean invert) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, validatorParams);
        log.info("Running {} validator[{}] with params: {}", invert ? "inverted " : "", this.getClass().getSimpleName(), properties.toString());
        if (twinEntity == null && !nullable()) {
            return buildResult(
                    false,
                    invert,
                    "given twin is null, next validation is skipped",
                    "given twin is not null");
        }

        int featurerId = this.getClass().getAnnotation(FeaturerType.class).id();
        String cacheKey = FeaturerService.toConfigKey(featurerId, validatorParams);

        if (twinEntity.getTwinValidatorResultCache() == null) {
            twinEntity.setTwinValidatorResultCache(new HashMap<>());
        }

        Boolean cachedResult = twinEntity.getTwinValidatorResultCache().get(cacheKey);
        if (cachedResult != null) {
            return buildResult(
                    cachedResult,
                    invert,
                    "cached validation failed",
                    "cached validation succeeded but inverted");
        }

        // Always validate without invert first and cache the result
        ValidationResult validationResult = isValid(properties, twinEntity, false);
        boolean isValidWithoutInvert = validationResult.isValid();

        twinEntity.getTwinValidatorResultCache().put(cacheKey, isValidWithoutInvert);
        log.info("Cached result for validator[{}], key: {}, result: {}", this.getClass().getSimpleName(), cacheKey, isValidWithoutInvert);

        // Apply invert if needed
        if (invert) {
            return buildResult(
                    isValidWithoutInvert,
                    true,
                    validationResult.getMessage(),
                    validationResult.getMessage());
        }
        return validationResult;
    }

    protected boolean nullable() {
        return false;
    }

    protected abstract ValidationResult isValid(Properties properties, TwinEntity twinEntity, boolean invert) throws ServiceException;

    public CollectionValidationResult isValid(HashMap<String, String> validatorParams, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, validatorParams);
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
