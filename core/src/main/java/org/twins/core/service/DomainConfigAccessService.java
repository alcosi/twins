package org.twins.core.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.cambium.common.util.ChangesHelper;
import org.cambium.common.util.ChangesHelperMulti;
import org.cambium.service.EntitySmartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.twins.core.dao.DomainConfigEntity;
import org.twins.core.dao.DomainConfigRepository;
import org.twins.core.dao.annotation.DomainIdFiller;
import org.twins.core.domain.DomainConfig;
import org.twins.core.service.domain.DomainVersionService;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;


@Slf4j
public abstract class DomainConfigAccessService<T extends DomainConfigEntity> extends TwinsEntityAccessService<T> {
    @Autowired
    protected TransactionActorUserService transactionActorUserService;

    @Autowired
    protected DomainVersionService domainVersionService;

    /**
     * Narrowed to {@link DomainConfigRepository}: every DomainConfigEntity repository exposes
     * the domain-filtered by-id reads used by the loadEntityById/loadEntitiesById overrides below.
     */
    @Override
    public abstract DomainConfigRepository<T> entityRepository();

    /**
     * Domain-scoped by-id read (TWINS-719): ids from other domains are invisible (same as
     * "not found"), system rows (domain_id IS NULL) stay visible — mirrors isDomainAccessDenied.
     */
    @Override
    protected T loadEntityById(UUID entityId, EntitySmartService.FindMode findMode) throws ServiceException {
        UUID domainId = currentDomainIdOrNull();
        if (domainId == null)
            return super.loadEntityById(entityId, findMode);
        return entitySmartService.checkOptional(entityRepository().findByDomainIdAndId(domainId, entityId), entityId, entityRepository(), findMode);
    }

    /**
     * Bulk variant of the domain-scoped read — one filtered query instead of per-id probes.
     * Mirrors {@link EntitySmartService#findByIdIn}, but the read is domain-filtered.
     */
    @Override
    protected Kit<T, UUID> loadEntitiesById(Collection<UUID> entityIds, EntitySmartService.ListFindMode findMode) throws ServiceException {
        UUID domainId = currentDomainIdOrNull();
        if (domainId == null)
            return super.loadEntitiesById(entityIds, findMode);
        if (CollectionUtils.isEmpty(entityIds)) // if someone pass null or empty list, we will create empty kit
            return new Kit<>(entityGetIdFunction());
        Kit<T, UUID> kit = new Kit<>(entityRepository().findAllByDomainIdAndIdIn(domainId, entityIds), entityGetIdFunction());
        for (UUID id : entityIds) {
            if (!kit.containsKey(id)) {
                switch (findMode) {
                    case ifMissedIgnore:
                        continue;
                    case ifMissedLog:
                        log.error("{} can not find entity with id[{}]", entitySmartService.entityShortName(entityRepository()), id);
                        continue;
                    case ifMissedThrows:
                        throw new ServiceException(ErrorCodeCommon.UUID_UNKNOWN, "unknown " + entitySmartService.entityShortName(entityRepository()) + "[" + id + "]");
                }
            }
        }
        return kit;
    }

    // null = no auth/domain context (system jobs, schedulers) — reads stay unfiltered
    private UUID currentDomainIdOrNull() {
        try {
            return authService.getApiUser().getDomainId();
        } catch (ServiceException e) {
            return null;
        }
    }

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

    public void loadDomainVersion(T src) throws ServiceException {
        loadDomainVersion(Collections.singletonList(src));
    }

    public void loadDomainVersion(Collection<T> srcCollection) throws ServiceException {
        domainVersionService.load(srcCollection,
                DomainConfigEntity::getDomainVersionId,
                DomainConfigEntity::getDomainVersion,
                DomainConfigEntity::setDomainVersion);
    }
}
