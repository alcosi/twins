package org.twins.core.featurer.twin.validator;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.featurer.FeaturerService;
import org.cambium.featurer.annotations.FeaturerType;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.featurer.FeaturerTwins;

import java.util.*;


@FeaturerType(id = FeaturerTwins.TYPE_16,
        name = "TwinValidator",
        description = "")
@Slf4j
public abstract class TwinValidator extends FeaturerTwins {

    public ValidationResult isValid(HashMap<String, String> validatorParams, TwinEntity twinEntity, boolean invert) throws ServiceException {
        var isValid = isValid(validatorParams, Collections.singletonList(twinEntity), invert);
        return isValid.getTwinsResults().get(twinEntity.getId());
    }

    protected boolean nullable() {
        return false;
    }

    public CollectionValidationResult isValid(HashMap<String, String> validatorParams, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException {
        Properties properties = featurerService.extractProperties(this, validatorParams);
        log.info("Running " + (invert ? "inverted " : "") + " validator[" + this.getClass().getSimpleName() + "] with params: " + properties.toString());

        String cacheKey = FeaturerService.toConfigKey(this, validatorParams);

        List<TwinEntity> twinsToValidate = new ArrayList<>();
        for (var twinEntity : twinEntityCollection) {
            if (twinEntity.getTwinValidatorResultCache() == null) {
                twinEntity.setTwinValidatorResultCache(new HashMap<>());
            }
            if (twinEntity.getTwinValidatorResultCache().get(cacheKey) == null) {
                twinsToValidate.add(twinEntity);
            }
        }

        // Validate only non-cached twins
        if (!twinsToValidate.isEmpty()) {
            CollectionValidationResult validationResult = isValid(properties, twinsToValidate, false);
            // Cache the results
            for (TwinEntity twinEntity : twinsToValidate) {
                ValidationResult result = validationResult.getTwinsResults().get(twinEntity.getId());
                if (result != null) {
                    twinEntity.getTwinValidatorResultCache().put(cacheKey, result.isValid());
                    log.info("Cached result for validator[{}], twin: {}, key: {}, result: {}", this.getClass().getSimpleName(), twinEntity.getId(), cacheKey, result.isValid());
                } else {
                    throw new ServiceException(ErrorCodeTwins.TWIN_VALIDATOR_INCORRECT, "validator [" + this.getClass().getSimpleName() + "] did not return result for " + twinEntity.logShort());
                }
            }
        }

        // Build final result from cache for all twins
        CollectionValidationResult collectionValidationResult = new CollectionValidationResult();
        for (var twinEntity : twinEntityCollection) {
            Boolean cachedResult = twinEntity.getTwinValidatorResultCache().get(cacheKey);
            ValidationResult result = buildResult(
                    cachedResult != null && cachedResult,
                    invert,
                    "cached validation failed",
                    "cached validation succeeded but inverted");
            collectionValidationResult.getTwinsResults().put(twinEntity.getId(), result);
        }
        return collectionValidationResult;
    }

    /**
     * Must be overridden to reduce db query count
     */
    protected abstract CollectionValidationResult isValid(Properties properties, Collection<TwinEntity> twinEntityCollection, boolean invert) throws ServiceException;


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
