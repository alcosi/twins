package org.twins.core.service;

import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.DomainConfigEntity;
import org.twins.core.dao.annotation.DomainIdFiller;
import org.twins.core.domain.DomainConfig;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;


public abstract class DomainConfigAccessService<T extends DomainConfigEntity> extends TwinsEntityAccessService<T> {
    @Autowired
    protected TransactionActorUserService transactionActorUserService;

    public BiFunction<UUID, String, Optional<T>> findByDomainIdAndKeyFunction() throws ServiceException {
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED, "Method findByKey is not implemented in service");
    }

    @Override
    public Optional<T> findByKey(String key) throws ServiceException {
        UUID domainId = authService.getApiUser().getDomainId();
        return findByDomainIdAndKeyFunction().apply(domainId, key);
    }

    /**
     * Universal {@code domain_id} autofill on insert (TWINS-719) — see {@link DomainIdFiller}.
     * Only {@link DomainConfig} entities are eligible; no resolvable domain context
     * (system jobs, schedulers) = entity untouched.
     */
    public <E> E autofillDomainId(E entity) {
        if (!(entity instanceof DomainConfig))
            return entity;  // not a versioned config row — skip without resolving apiUser
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
        transactionActorUserService.fillCurrentActorUser();
        return super.saveSafe(autofillDomainId(entity));
    }

    @Override
    public Iterable<T> saveSafe(Collection<T> entities) throws ServiceException {
        transactionActorUserService.fillCurrentActorUser();
        return super.saveSafe(autofillDomainIds(entities));
    }

    @Override
    public T updateSafe(T entity, ChangesHelper changesHelper) throws ServiceException {
        transactionActorUserService.fillCurrentActorUser();
        return super.updateSafe(entity, changesHelper);
    }

    @Override
    public Iterable<T> updateSafe(ChangesHelperMulti<T> changesHelperMulti) throws ServiceException {
        transactionActorUserService.fillCurrentActorUser();
        return super.updateSafe(changesHelperMulti);
    }

    @Override
    public void deleteSafe(UUID id) throws ServiceException {
        transactionActorUserService.fillCurrentActorUser();
        super.deleteSafe(id);
    }

    @Override
    public void deleteSafe(Collection<UUID> ids) throws ServiceException {
        transactionActorUserService.fillCurrentActorUser();
        super.deleteSafe(ids);
    }
}
