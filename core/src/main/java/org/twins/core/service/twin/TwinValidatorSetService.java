package org.twins.core.service.twin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.kit.KitGrouped;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.validator.TwinValidatorSetEntity;
import org.twins.core.dao.validator.TwinValidatorSetRepository;
import org.twins.core.dao.validator.ContainsTwinValidatorSet;
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
}
