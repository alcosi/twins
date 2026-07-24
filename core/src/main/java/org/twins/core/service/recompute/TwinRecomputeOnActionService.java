package org.twins.core.service.recompute;

import io.github.breninsul.logging.aspect.JavaLoggingLevel;
import io.github.breninsul.logging.aspect.annotation.LogExecutionTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cambium.common.exception.ServiceException;
import org.cambium.service.EntitySmartService;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;
import org.twins.core.dao.recompute.TwinRecomputeOnActionEntity;
import org.twins.core.dao.recompute.TwinRecomputeOnActionRepository;
import org.twins.core.dao.recompute.TwinRecomputeSubscriberEntity;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Secure-find service for {@link TwinRecomputeOnActionEntity} — OnAction recompute rules. Hot-path lookups
 * {@link #findByPublisherTwinClassIdIn(Collection)} are cached at the repository level
 * (see {@link TwinRecomputeOnActionRepository#CACHE_BY_PUBLISHER_CLASS_IN}); this service is the thin
 * secure-find wrapper the orchestrator goes through.
 *
 * The {@code load*} methods delegate to the base {@link org.cambium.service.EntitySecureFindServiceImpl#load}
 * on the related entity's service — one batch SQL per relationship, no N+1.
 */
@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinRecomputeOnActionService extends TwinsEntitySecureFindService<TwinRecomputeOnActionEntity> {
    private final TwinRecomputeOnActionRepository repository;
    @Lazy
    private final TwinRecomputeSubscriberService twinRecomputeSubscriberService;
    @Lazy
    private final TwinClassService twinClassService;

    @Override
    public CrudRepository<TwinRecomputeOnActionEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinRecomputeOnActionEntity, UUID> entityGetIdFunction() {
        return TwinRecomputeOnActionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinRecomputeOnActionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinRecomputeOnActionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logShort() + " empty id");
        if (entity.getRecomputeSubscriberId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty recomputeSubscriberId");
        if (entity.getPublisherTwinClassId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty publisherTwinClassId");
        if (entity.getPublisherTwinAction() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty publisherTwinAction");
        return true;
    }

    public List<TwinRecomputeOnActionEntity> findByPublisherTwinClassIdIn(Collection<UUID> publisherTwinClassIds) {
        if (publisherTwinClassIds == null || publisherTwinClassIds.isEmpty())
            return List.of();
        return repository.findByPublisherTwinClassIdIn(publisherTwinClassIds);
    }

    public void loadSubscriber(TwinRecomputeOnActionEntity src) throws ServiceException {
        loadSubscriber(Collections.singleton(src));
    }

    /**
     * Attaches the subscriber parent to each rule (one batch SQL via the base {@code load} helper on
     * {@link TwinRecomputeSubscriberService}), then hydrates the distinct subscribers' twin pointer +
     * twin class field in one batch SQL each.
     */
    public void loadSubscriber(Collection<TwinRecomputeOnActionEntity> srcCollection) throws ServiceException {
        twinRecomputeSubscriberService.load(srcCollection,
                TwinRecomputeOnActionEntity::getRecomputeSubscriberId,
                TwinRecomputeOnActionEntity::getSubscriber,
                TwinRecomputeOnActionEntity::setSubscriber);
        List<TwinRecomputeSubscriberEntity> subscribers = srcCollection.stream()
                .map(TwinRecomputeOnActionEntity::getSubscriber)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (subscribers.isEmpty())
            return;
        twinRecomputeSubscriberService.loadSubscriberTwinPointer(subscribers);
        twinRecomputeSubscriberService.loadSubscriberTwinClassField(subscribers);
    }

    public void loadPublisherTwinClass(TwinRecomputeOnActionEntity src) throws ServiceException {
        loadPublisherTwinClass(Collections.singleton(src));
    }

    public void loadPublisherTwinClass(Collection<TwinRecomputeOnActionEntity> srcCollection) throws ServiceException {
        twinClassService.load(srcCollection,
                TwinRecomputeOnActionEntity::getPublisherTwinClassId,
                TwinRecomputeOnActionEntity::getPublisherTwinClass,
                TwinRecomputeOnActionEntity::setPublisherTwinClass);
    }
}
