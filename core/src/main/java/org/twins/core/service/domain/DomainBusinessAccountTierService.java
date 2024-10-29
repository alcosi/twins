package org.twins.core.service.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.domain.*;
import org.twins.core.exception.ErrorCodeTwins;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Lazy
@RequiredArgsConstructor
public class DomainBusinessAccountTierService extends EntitySecureFindServiceImpl<TierEntity> {

    private final TierRepository tierRepository;

    @Override
    public CrudRepository<TierEntity, UUID> entityRepository() {
        return tierRepository;
    }

    @Override
    public boolean isEntityReadDenied(TierEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TierEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        return true;
    }

    public UUID checkTierAllowed(UUID domainTierId, UUID domainId) throws ServiceException{
        Optional<TierEntity> domainBusinessAccountTierEntity = tierRepository.findById(domainTierId);
        if (domainBusinessAccountTierEntity.isEmpty())
            throw new ServiceException(ErrorCodeTwins.UUID_UNKNOWN, "unknown domainTierId[" + domainTierId + "]");
        if (!domainBusinessAccountTierEntity.get().getDomainId().equals(domainId))
            throw new ServiceException(ErrorCodeTwins.TIER_NOT_ALLOWED, "domainTierId[" + domainTierId + "] is not allows in domain[" + domainId + "]");
        return domainTierId;

    }
}
