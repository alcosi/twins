package org.twins.core.service.twinclassfield.recompute;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.twinclassfieldrecompute.TwinClassFieldRecomputeOnFieldEntity;
import org.twins.core.dao.twinclassfieldrecompute.TwinClassFieldRecomputeOnFieldRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.twin.TwinPointerService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Secure-find service for {@link TwinClassFieldRecomputeOnFieldEntity} — OnField recompute rules
 * ("when publisher field X changes, recompute subscriber field Y of the twin pointed to by Z").
 *
 * Hot-path lookup {@link #findByPublisherTwinClassFieldIdIn(Collection)} is cached at the repository
 * level (see {@link TwinClassFieldRecomputeOnFieldRepository#CACHE_BY_PUBLISHER_FIELD_IN}); this service
 * is a thin secure-find wrapper that the orchestrator and the future admin UI both go through.
 *
 * The {@code load*} methods delegate to the base {@link org.cambium.service.EntitySecureFindServiceImpl#load}
 * on the related entity's service — one batch SQL per relationship, no N+1 in mappers / admin UI.
 *
 * MVP registration is via direct SQL INSERT (see ai/plans/field-typer-mater-listeners.md §3); cache
 * invalidation therefore relies on Caffeine's 5-minute TTL rather than explicit @CacheEvict, which
 * will be wired up together with the admin CRUD endpoints.
 */
@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldRecomputeOnFieldService extends TwinsEntitySecureFindService<TwinClassFieldRecomputeOnFieldEntity> {
    private final TwinClassFieldRecomputeOnFieldRepository repository;
    @Lazy
    private final TwinPointerService twinPointerService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public CrudRepository<TwinClassFieldRecomputeOnFieldEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinClassFieldRecomputeOnFieldEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldRecomputeOnFieldEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldRecomputeOnFieldEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldRecomputeOnFieldEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logShort() + " empty id");
        if (entity.getSubscriberTwinPointerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty subscriberTwinPointerId");
        if (entity.getSubscriberTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty subscriberTwinClassFieldId");
        if (entity.getPublisherTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty publisherTwinClassFieldId");
        return true;
    }

    public List<TwinClassFieldRecomputeOnFieldEntity> findByPublisherTwinClassFieldIdIn(Collection<UUID> publisherFieldIds) {
        if (publisherFieldIds == null || publisherFieldIds.isEmpty())
            return List.of();
        return repository.findByPublisherTwinClassFieldIdIn(publisherFieldIds);
    }

    public void loadSubscriberTwinPointer(TwinClassFieldRecomputeOnFieldEntity src) throws ServiceException {
        loadSubscriberTwinPointer(Collections.singleton(src));
    }

    public void loadSubscriberTwinPointer(Collection<TwinClassFieldRecomputeOnFieldEntity> srcCollection) throws ServiceException {
        twinPointerService.load(srcCollection,
                TwinClassFieldRecomputeOnFieldEntity::getSubscriberTwinPointerId,
                TwinClassFieldRecomputeOnFieldEntity::getSubscriberTwinPointer,
                TwinClassFieldRecomputeOnFieldEntity::setSubscriberTwinPointer);
    }

    public void loadSubscriberTwinClassField(TwinClassFieldRecomputeOnFieldEntity src) throws ServiceException {
        loadSubscriberTwinClassField(Collections.singleton(src));
    }

    public void loadSubscriberTwinClassField(Collection<TwinClassFieldRecomputeOnFieldEntity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                TwinClassFieldRecomputeOnFieldEntity::getSubscriberTwinClassFieldId,
                TwinClassFieldRecomputeOnFieldEntity::getSubscriberTwinClassField,
                TwinClassFieldRecomputeOnFieldEntity::setSubscriberTwinClassField);
    }

    public void loadPublisherTwinClassField(TwinClassFieldRecomputeOnFieldEntity src) throws ServiceException {
        loadPublisherTwinClassField(Collections.singleton(src));
    }

    public void loadPublisherTwinClassField(Collection<TwinClassFieldRecomputeOnFieldEntity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                TwinClassFieldRecomputeOnFieldEntity::getPublisherTwinClassFieldId,
                TwinClassFieldRecomputeOnFieldEntity::getPublisherTwinClassField,
                TwinClassFieldRecomputeOnFieldEntity::setPublisherTwinClassField);
    }
}
