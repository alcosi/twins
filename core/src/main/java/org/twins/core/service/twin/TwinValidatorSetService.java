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
        loadTwinValidatorSet(validatorContainer);
        if (validatorContainer.getTwinValidatorSet() == null) {
            return true;
        }
        twinValidatorService.loadValidators(validatorContainer);
        if (validatorContainer.getTwinValidatorKit() == null) {
            return !validatorContainer.getTwinValidatorSet().isInvert();
        }
        List<TwinValidatorEntity> sortedTwinValidators = new ArrayList<>(validatorContainer.getTwinValidatorKit().getList());
        sortedTwinValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));
        boolean validationResultOfSet = true;
        for (TwinValidatorEntity twinValidatorEntity : sortedTwinValidators) {
            if (!twinValidatorEntity.isActive()) {
                log.info("{} from {} will not be used, since it is inactive.", twinValidatorEntity.logNormal(), validatorContainer.logNormal());
                continue;
            }

            TwinValidator twinValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturerId(), TwinValidator.class);
            ValidationResult validationResult = twinValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinEntity, twinValidatorEntity.isInvert());
            validationResultOfSet = validationResult.isValid();

            if (!validationResultOfSet) {
                log.info("{} from {} is not valid. {}", twinValidatorEntity.logNormal(), validatorContainer.logNormal(), validationResult.getMessage());
                break;
            }
        }

        return validatorContainer.getTwinValidatorSet().isInvert() != validationResultOfSet;
    }

    public <T extends ContainsTwinValidatorSet> Map<UUID, ValidationResult> isValid(Collection<TwinEntity> twinEntities, T validatorContainer) throws ServiceException {
        loadTwinValidatorSet(validatorContainer);

        if (validatorContainer.getTwinValidatorSet() == null) {
            return createAllValidResults(twinEntities);
        }

        twinValidatorService.loadValidators(validatorContainer);
        boolean setInvert = validatorContainer.getTwinValidatorSet().isInvert();

        if (validatorContainer.getTwinValidatorKit() == null) {
            return createResultsWithSetInvert(twinEntities, setInvert);
        }

        List<TwinValidatorEntity> sortedValidators = getSortedActiveValidators(validatorContainer);
        Map<UUID, ValidationResult> results = initializeResults(twinEntities);
        List<TwinEntity> remainingTwins = new ArrayList<>(twinEntities);

        for (TwinValidatorEntity validatorEntity : sortedValidators) {
            if (!validatorEntity.isActive()) {
                log.info("{} from {} will not be used, since it is inactive.", validatorEntity.logNormal(), validatorContainer.logNormal());
                continue;
            }
            TwinValidator twinValidator = featurerService.getFeaturer(validatorEntity.getTwinValidatorFeaturerId(), TwinValidator.class);
            TwinValidator.CollectionValidationResult validationResultCollection = twinValidator.isValid(validatorEntity.getTwinValidatorParams(), remainingTwins, validatorEntity.isInvert());

            List<TwinEntity> validTwins = new ArrayList<>();
            for (TwinEntity twin : remainingTwins) {
                ValidationResult result = validationResultCollection.getTwinsResults().get(twin.getId());
                if (result.isValid()) {
                    validTwins.add(twin);
                }
                results.put(twin.getId(), result);
            }

            remainingTwins = validTwins;
            if (remainingTwins.isEmpty()) {
                break;
            }
        }

        if (setInvert) {
            invertResults(results);
        }

        return results;
    }

    private Map<UUID, ValidationResult> createAllValidResults(Collection<TwinEntity> twinEntities) {
        return twinEntities.stream()
                .collect(Collectors.toMap(
                        TwinEntity::getId,
                        t -> new ValidationResult().setValid(true)
                ));
    }

    private Map<UUID, ValidationResult> createResultsWithSetInvert(Collection<TwinEntity> twinEntities, boolean setInvert) {
        return twinEntities.stream()
                .collect(Collectors.toMap(
                        TwinEntity::getId,
                        t -> new ValidationResult().setValid(!setInvert)
                ));
    }

    private List<TwinValidatorEntity> getSortedActiveValidators(ContainsTwinValidatorSet validatorContainer) {
        List<TwinValidatorEntity> validators = new ArrayList<>(
                validatorContainer.getTwinValidatorKit().getList()
        );
        validators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));
        return validators;
    }

    private Map<UUID, ValidationResult> initializeResults(Collection<TwinEntity> twinEntities) {
        Map<UUID, ValidationResult> results = new HashMap<>();
        for (TwinEntity twin : twinEntities) {
            results.put(twin.getId(), new ValidationResult().setValid(true));
        }
        return results;
    }


    private void invertResults(Map<UUID, ValidationResult> results) {
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
