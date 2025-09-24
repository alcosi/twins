package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.EasyLoggable;
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

import java.util.*;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinValidatorSetService extends EntitySecureFindServiceImpl<TwinValidatorSetEntity> {

    private final TwinValidatorSetRepository twinValidatorSetRepository;
    @Lazy
    private final AuthService authService;
    private final FeaturerService featurerService;

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

    public <T extends ContainsTwinValidatorSet> TwinValidatorSetEntity loadTwinValidatorSet(T entity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (entity.getTwinValidatorSet() != null)
            return entity.getTwinValidatorSet();
        entity.setTwinValidatorSet(twinValidatorSetRepository.findAllByIdAndDomainId(entity.getTwinValidatorSetId(), apiUser.getDomainId()));
        return entity.getTwinValidatorSet();
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

    public boolean isValid(TwinEntity twinEntity, EasyLoggable validationForEntity, Collection<TwinValidatorEntity> validatorsSet) throws ServiceException {
        List<TwinValidatorEntity> sortedTwinValidators = new ArrayList<>(validatorsSet);
        sortedTwinValidators.sort(Comparator.comparing(TwinValidatorEntity::getOrder));
        boolean validationResultOfSet = true;
        for (TwinValidatorEntity twinValidatorEntity : sortedTwinValidators) {
            if (!twinValidatorEntity.isActive()) {
                log.info("{} from {} will not be used, since it is inactive. ", twinValidatorEntity.logNormal(), validationForEntity.logNormal());
                continue;
            }

            TwinValidator transitionValidator = featurerService.getFeaturer(twinValidatorEntity.getTwinValidatorFeaturer(), TwinValidator.class);
            TwinValidator.ValidationResult validationResult = transitionValidator.isValid(twinValidatorEntity.getTwinValidatorParams(), twinEntity, twinValidatorEntity.isInvert());
            validationResultOfSet = validationResult.isValid();
            if (!validationResultOfSet) {
                log.info("{} from {} is not valid. {}", twinValidatorEntity.logNormal(), validationForEntity.logNormal(), validationResult.getMessage());
                break;
            }
        }
        return validationResultOfSet;
    }
}
