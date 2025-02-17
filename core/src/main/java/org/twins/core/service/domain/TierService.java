package org.twins.core.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dao.domain.TierRepository;
import org.twins.core.domain.ApiUser;
import org.twins.core.exception.ErrorCodeTwins;
import org.twins.core.service.auth.AuthService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class TierService extends EntitySecureFindServiceImpl<TierEntity> {

    private final TierRepository tierRepository;
    @Lazy
    private final AuthService authService;


    @Override
    public CrudRepository<TierEntity, UUID> entityRepository() {
        return tierRepository;
    }

    @Override
    public Function<TierEntity, UUID> entityGetIdFunction() {
        return TierEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TierEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TierEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        ApiUser apiUser = authService.getApiUser();
        if (!entity.getDomainId().equals(apiUser.getDomainId()))
            return logErrorAndReturnFalse("domainTierId[" + entity.getId() + "] is not allows in domain[" + apiUser.getDomainId() + "]");
        return true;
    }

    public UUID checkTierAllowed(UUID domainTierId) throws ServiceException{
        Optional<TierEntity> domainBusinessAccountTierEntity = tierRepository.findById(domainTierId);
        if (domainBusinessAccountTierEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown domainTierId[" + domainTierId + "]");
        validateEntityAndThrow(domainBusinessAccountTierEntity.get(), EntitySmartService.EntityValidateMode.beforeSave);
        return domainTierId;
    }
}
