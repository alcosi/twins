package org.twins.core.service;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntityAccessServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.service.auth.AuthService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;


public abstract class TwinsEntityAccessService<T> extends EntityAccessServiceImpl<T> {
    @Autowired
    protected AuthService authService;

    public BiFunction<UUID, String, Optional<T>> findByDomainIdAndKeyFunction() throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "Method findByKey is not implemented in service");
    }

    @Override
    public Optional<T> findByKey(String key) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        return findByDomainIdAndKeyFunction().apply(domainId, key);
    }

    protected boolean isDomainAccessDenied(UUID entityDomainId) throws ServiceException {
        if (entityDomainId == null) {
            return false;
        }
        var currentDomainId = authService.getApiUser().getDomainId();
        return !entityDomainId.equals(currentDomainId);
    }

    protected boolean checkDomainAccessDenied(UUID entityDomainId, String entityLog, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        if (isDomainAccessDenied(entityDomainId)) {
            EntitySmartService.entityReadDenied(readPermissionCheckMode, entityLog + " is not allowed in current domain");
            return true;
        }
        return false;
    }
}
