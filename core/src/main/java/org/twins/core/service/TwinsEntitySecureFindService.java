package org.twins.core.service;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySecureFindServiceImpl;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.annotation.DomainIdFiller;
import org.twins.core.service.auth.AuthService;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;


public abstract class TwinsEntitySecureFindService<T> extends EntitySecureFindServiceImpl<T> {
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

    /**
     * Universal {@code domain_id} autofill on insert (TWINS-719) — see {@link DomainIdFiller}.
     * No resolvable domain context (system jobs, schedulers) = entity untouched.
     */
    public <E> E autofillDomainId(E entity) {
        UUID domainId;
        try {
            domainId = authService.getApiUser().getDomainId();
        } catch (ServiceException e) {
            // system path without a domain context — leave the entity as is
            return entity;
        }
        return DomainIdFiller.fillOnInsert(entity, domainId);
    }

    public <E> Collection<E> autofillDomainIds(Collection<E> entities) {
        UUID domainId;
        try {
            domainId = authService.getApiUser().getDomainId();
        } catch (ServiceException e) {
            return entities;
        }
        DomainIdFiller.fillOnInsert(entities, domainId);
        return entities;
    }

    @Override
    public T saveSafe(T entity) throws ServiceException {
        return super.saveSafe(autofillDomainId(entity));
    }

    @Override
    public Iterable<T> saveSafe(Collection<T> entities) throws ServiceException {
        return super.saveSafe(autofillDomainIds(entities));
    }
}
