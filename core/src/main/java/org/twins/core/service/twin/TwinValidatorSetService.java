package org.twins.core.service.twin;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.ValidationResult;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.featurer.FeaturerService;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dao.validator.TwinValidatorSetRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.featurer.twin.validator.TwinValidator;
import org.twins.core.service.auth.AuthService;
import org.twins.core.service.validator.TwinValidatorService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinValidatorSetService extends EntitySecureFindServiceImpl<TwinValidatorSetEntity> {

    private final TwinValidatorSetRepository twinValidatorSetRepository;
    @Lazy
    private final AuthService authService;
    private final FeaturerService featurerService;
    private final TwinValidatorService twinValidatorService;

    @Override
    public CrudRepository<TwinValidatorSetEntity, UUID> entityRepository() {
        return twinValidatorSetRepository;
    }

    @Override
    public Function<TwinValidatorSetEntity, UUID> entityGetIdFunction() {
        return TwinValidatorSetEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinValidatorSetEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinValidatorSetEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public <T extends ContainsTwinValidatorSet> void loadTwinValidatorSet(T entity) throws ServiceException {
        loadTwinValidatorSet(Collections.singletonList(entity));
    }

    public <T extends ContainsTwinValidatorSet> void loadTwinValidatorSet(Collection<T> implementedValidatorRules) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        KitGrouped<T, UUID, UUID> needLoad = new KitGrouped<>(T::getId, T::getTwinValidatorSetId);
        for (T validatorRule : implementedValidatorRules)
            if (validatorRule.getTwinValidatorSet() == null) {
                needLoad.add(validatorRule);
            }
        if (needLoad.isEmpty())
            return;
        Kit<TwinValidatorSetEntity, UUID> twinValidatorSetEntitiesKit = new Kit<>(twinValidatorSetRepository.findAllByIdInAndDomainId(needLoad.getGroupedMap().keySet(), apiUser.getDomainId()), TwinValidatorSetEntity::getId);
        if (CollectionUtils.isEmpty(twinValidatorSetEntitiesKit.getCollection()))
            return;
        for (T validatorRule : needLoad.getCollection())
            validatorRule.setTwinValidatorSet(twinValidatorSetEntitiesKit.get(validatorRule.getTwinValidatorSetId()));
    }

    public <T extends ContainsTwinValidatorSet> boolean isValid(TwinEntity twinEntity, T validatorContainer) throws ServiceException {
        Boolean validationReady = checkValidationReady(validatorContainer);
        if (validationReady == null) {
            return true;
        }
        if (!validationReady) {
            return !isSetInverted(validatorContainer);
        }

        List<TwinValidatorEntity> activeValidators = getSortedActiveValidators(validatorContainer);
        if (activeValidators.isEmpty()) {
            return !isSetInverted(validatorContainer);
        }

        boolean validationPassed = performValidation(twinEntity, activeValidators, validatorContainer);

        return isSetInverted(validatorContainer) != validationPassed;
    }

    public <T extends ContainsTwinValidatorSet> Map<UUID, ValidationResult> isValid(Collection<TwinEntity> twinEntities, T validatorContainer) throws ServiceException {
        Boolean validationReady = checkValidationReady(validatorContainer);
        if (validationReady == null) {
            return createDefaultResults(twinEntities, true);
        }
        if (!validationReady) {
            return createDefaultResults(twinEntities, !isSetInverted(validatorContainer));
        }

        List<TwinValidatorEntity> activeValidators = getSortedActiveValidators(validatorContainer);
        if (activeValidators.isEmpty()) {
            return createDefaultResults(twinEntities, !isSetInverted(validatorContainer));
        }

        Map<UUID, ValidationResult> results = performCollectionValidation(twinEntities, activeValidators, validatorContainer);

        if (isSetInverted(validatorContainer)) {
            applyInversionToResults(results);
        }

        return results;
    }

    private <T extends ContainsTwinValidatorSet> Boolean checkValidationReady(T validatorContainer) throws ServiceException {
        loadTwinValidatorSet(validatorContainer);
        if (validatorContainer.getTwinValidatorSet() == null) {
            return null;
        }

        twinValidatorService.loadValidators(validatorContainer);
        return validatorContainer.getTwinValidatorKit() != null;
    }

    private <T extends ContainsTwinValidatorSet> boolean isSetInverted(T validatorContainer) {
        return validatorContainer.getTwinValidatorSet() != null && validatorContainer.getTwinValidatorSet().isInvert();
    }

    private <T extends ContainsTwinValidatorSet> List<TwinValidatorEntity> getSortedActiveValidators(T validatorContainer) {
        Kit<TwinValidatorEntity, UUID> validatorKit = validatorContainer.getTwinValidatorKit();
        if (validatorKit == null) {
            return Collections.emptyList();
        }

        List<TwinValidatorEntity> sortedValidators = new ArrayList<>(validatorKit.getList());
        sortedValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));

        List<TwinValidatorEntity> activeValidators = new ArrayList<>();
        for (TwinValidatorEntity validator : sortedValidators) {
            if (validator.isActive()) {
                activeValidators.add(validator);
            } else {
                log.info("{} from {} will not be used, since it is inactive.", validator.logNormal(), validatorContainer.logNormal());
            }
        }

        return activeValidators;
    }

    private <T extends ContainsTwinValidatorSet> boolean performValidation(TwinEntity twinEntity, List<TwinValidatorEntity> activeValidators, T validatorContainer) throws ServiceException {
        for (TwinValidatorEntity validatorEntity : activeValidators) {
            if (!validateWithSingleValidator(twinEntity, validatorEntity, validatorContainer)) {
                return false;
            }
        }

        return true;
    }

    private <T extends ContainsTwinValidatorSet> boolean validateWithSingleValidator(TwinEntity twinEntity, TwinValidatorEntity validatorEntity, T validatorContainer) throws ServiceException {
        TwinValidator validator = featurerService.getFeaturer(validatorEntity.getTwinValidatorFeaturerId(), TwinValidator.class);
        ValidationResult result = validator.isValid(validatorEntity.getTwinValidatorParams(), twinEntity, validatorEntity.isInvert());

        if (!result.isValid()) {
            log.info("{} from {} is not valid. {}", validatorEntity.logNormal(), validatorContainer.logNormal(), result.getMessage());
            return false;
        }

        return true;
    }

    private Map<UUID, ValidationResult> createDefaultResults(Collection<TwinEntity> twinEntities, boolean isValid) {
        Map<UUID, ValidationResult> results = new HashMap<>();
        for (TwinEntity twin : twinEntities) {
            results.put(twin.getId(), new ValidationResult().setValid(isValid));
        }
        return results;
    }

    private <T extends ContainsTwinValidatorSet> Map<UUID, ValidationResult> performCollectionValidation(Collection<TwinEntity> twinEntities, List<TwinValidatorEntity> activeValidators, T validatorContainer) throws ServiceException {
        Map<UUID, ValidationResult> results = initializeResultsMap(twinEntities);

        for (TwinValidatorEntity validatorEntity : activeValidators) {
            List<TwinEntity> stillValidTwins = getStillValidTwins(twinEntities, results);
            if (stillValidTwins.isEmpty()) {
                break;
            }

            validateTwinsWithValidator(stillValidTwins, validatorEntity, results);
        }

        return results;
    }

    private Map<UUID, ValidationResult> initializeResultsMap(Collection<TwinEntity> twinEntities) {
        Map<UUID, ValidationResult> results = new HashMap<>();
        for (TwinEntity twin : twinEntities) {
            results.put(twin.getId(), new ValidationResult().setValid(true));
        }
        return results;
    }

    private List<TwinEntity> getStillValidTwins(Collection<TwinEntity> twinEntities, Map<UUID, ValidationResult> results) {
        List<TwinEntity> stillValidTwins = new ArrayList<>();
        for (TwinEntity twin : twinEntities) {
            if (results.get(twin.getId()).isValid()) {
                stillValidTwins.add(twin);
            }
        }
        return stillValidTwins;
    }

    private <T extends ContainsTwinValidatorSet> void validateTwinsWithValidator(List<TwinEntity> twinsToValidate, TwinValidatorEntity validatorEntity, Map<UUID, ValidationResult> results) throws ServiceException {
        TwinValidator validator = featurerService.getFeaturer(
                validatorEntity.getTwinValidatorFeaturerId(), TwinValidator.class);

        TwinValidator.CollectionValidationResult validatorResults = validator.isValid(
                validatorEntity.getTwinValidatorParams(),
                twinsToValidate,
                validatorEntity.isInvert());

        updateValidationResults(twinsToValidate, results, validatorResults);
    }

    private void updateValidationResults(List<TwinEntity> validatedTwins, Map<UUID, ValidationResult> results, TwinValidator.CollectionValidationResult validatorResults) {
        for (TwinEntity twin : validatedTwins) {
            ValidationResult validatorResult = validatorResults.getTwinsResults().get(twin.getId());
            if (validatorResult != null && !validatorResult.isValid()) {
                results.put(twin.getId(), validatorResult);
            }
        }
    }

    private void applyInversionToResults(Map<UUID, ValidationResult> results) {
        for (Map.Entry<UUID, ValidationResult> entry : results.entrySet()) {
            ValidationResult original = entry.getValue();
            entry.setValue(
                    new ValidationResult()
                            .setValid(!original.isValid())
                            .setMessage(original.getMessage())
            );
        }
    }

    public void loadTwinValidator(TwinValidatorEntity src) {
        loadTwinValidators(Collections.singletonList(src));
    }

    public void loadTwinValidators(Collection<TwinValidatorEntity> srcCollection) {
        featurerService.loadFeaturers(srcCollection,
                TwinValidatorEntity::getId,
                TwinValidatorEntity::getTwinValidatorFeaturerId,
                TwinValidatorEntity::getTwinValidatorFeaturer,
                TwinValidatorEntity::setTwinValidatorFeaturer);
    }
}
