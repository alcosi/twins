package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinValidatorEntity;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dao.validator.TwinValidatorSetRepository;
import org.twins.core.dao.validator.ValidatorRule;
import org.twins.core.domain.ApiUser;
import org.twins.core.service.auth.AuthService;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TwinValidatorSetService extends EntitySecureFindServiceImpl<TwinValidatorSetEntity> {

    private final TwinValidatorSetRepository twinValidatorSetRepository;
    @Lazy
    private final AuthService authService;

    @Override
    public CrudRepository<TwinValidatorSetEntity, UUID> entityRepository() {
        return twinValidatorSetRepository;
    }

    @Override
    public boolean isEntityReadDenied(TwinValidatorSetEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinValidatorSetEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public TwinValidatorSetEntity loadTwinValidatorSet(TwinValidatorEntity twinValidatorEntity) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (twinValidatorEntity.getTwinValidatorSet() != null)
            return twinValidatorEntity.getTwinValidatorSet();
        twinValidatorEntity.setTwinValidatorSet(twinValidatorSetRepository.findAllByIdAndDomainId(twinValidatorEntity.getTwinValidatorSetId(), apiUser.getDomainId()));
        return twinValidatorEntity.getTwinValidatorSet();
    }

    public TwinValidatorSetEntity loadTwinValidatorSet(ValidatorRule implementedValidatorRule) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (implementedValidatorRule.getTwinValidatorSet() != null)
            return implementedValidatorRule.getTwinValidatorSet();
        implementedValidatorRule.setTwinValidatorSet(twinValidatorSetRepository.findAllByIdAndDomainId(implementedValidatorRule.getTwinValidatorSetId(), apiUser.getDomainId()));
        return implementedValidatorRule.getTwinValidatorSet();
    }

    public void loadTwinValidatorSetForValidators(Collection<ValidatorRule> implementedValidatorRules) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Map<UUID, List<ValidatorRule>> needLoad = new HashMap<>();
        for (ValidatorRule validatorRule : implementedValidatorRules)
            if (validatorRule.getTwinValidatorSet() == null) {
                needLoad.computeIfAbsent(validatorRule.getTwinValidatorSetId(), k -> new ArrayList<>());
                needLoad.get(validatorRule.getTwinValidatorSetId()).add(validatorRule);
            }
        if (needLoad.isEmpty())
            return;
        Kit<TwinValidatorSetEntity, UUID> twinValidatorSetEntitiesKit = new Kit<>(twinValidatorSetRepository.findAllByIdInAndDomainId(needLoad.keySet(), apiUser.getDomainId()), TwinValidatorSetEntity::getId);
        if (CollectionUtils.isEmpty(twinValidatorSetEntitiesKit.getCollection()))
            return;
        for (Map.Entry<UUID, List<ValidatorRule>> entry : needLoad.entrySet())
            for (ValidatorRule validatorRule : entry.getValue())
                validatorRule.setTwinValidatorSet(twinValidatorSetEntitiesKit.get(entry.getKey()));
    }

    public void loadTwinValidatorSetForTwinValidators(Collection<TwinValidatorEntity> twinValidatorEntities) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        Map<UUID, List<TwinValidatorEntity>> needLoad = new HashMap<>();
        for (TwinValidatorEntity twinValidatorEntity : twinValidatorEntities)
            if (twinValidatorEntity.getTwinValidatorSet() == null) {
                needLoad.computeIfAbsent(twinValidatorEntity.getTwinValidatorSetId(), k -> new ArrayList<>());
                needLoad.get(twinValidatorEntity.getTwinValidatorSetId()).add(twinValidatorEntity);
            }
        if (needLoad.isEmpty())
            return;
        Kit<TwinValidatorSetEntity, UUID> twinValidatorSetEntitiesKit = new Kit<>(twinValidatorSetRepository.findAllByIdInAndDomainId(needLoad.keySet(), apiUser.getDomainId()), TwinValidatorSetEntity::getId);
        if (CollectionUtils.isEmpty(twinValidatorSetEntitiesKit.getCollection()))
            return;
        for (Map.Entry<UUID, List<TwinValidatorEntity>> entry : needLoad.entrySet())
            for (TwinValidatorEntity twinValidatorEntity : entry.getValue())
                twinValidatorEntity.setTwinValidatorSet(twinValidatorSetEntitiesKit.get(entry.getKey()));
    }
}
