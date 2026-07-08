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
import org.twins.core.dao.twinclassfieldrecompute.TwinClassFieldRecomputeOnActionEntity;
import org.twins.core.dao.twinclassfieldrecompute.TwinClassFieldRecomputeOnActionRepository;
import org.twins.core.enums.action.TwinAction;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.twin.TwinPointerService;
import org.twins.core.service.twinclass.TwinClassFieldService;
import org.twins.core.service.twinclass.TwinClassService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Secure-find service for {@link TwinClassFieldRecomputeOnActionEntity} — OnAction recompute rules
 * ("when TwinAction A is performed on a twin of class C, recompute subscriber field Y of the twin
 * pointed to by Z").
 *
 * Hot-path lookup is cached at the repository level
 * (see {@link TwinClassFieldRecomputeOnActionRepository#CACHE_BY_PUBLISHER_CLASS_ACTION}); this service
 * is a thin secure-find wrapper for the orchestrator and the future admin UI.
 *
 * The {@code load*} methods delegate to the base {@link org.cambium.service.EntitySecureFindServiceImpl#load}
 * on the related entity's service — one batch SQL per relationship, no N+1 in mappers / admin UI.
 */
@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinClassFieldRecomputeOnActionService extends TwinsEntitySecureFindService<TwinClassFieldRecomputeOnActionEntity> {
    private final TwinClassFieldRecomputeOnActionRepository repository;
    @Lazy
    private final TwinPointerService twinPointerService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;
    @Lazy
    private final TwinClassService twinClassService;

    @Override
    public CrudRepository<TwinClassFieldRecomputeOnActionEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinClassFieldRecomputeOnActionEntity, UUID> entityGetIdFunction() {
        return TwinClassFieldRecomputeOnActionEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinClassFieldRecomputeOnActionEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinClassFieldRecomputeOnActionEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logShort() + " empty id");
        if (entity.getSubscriberTwinPointerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty subscriberTwinPointerId");
        if (entity.getSubscriberTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty subscriberTwinClassFieldId");
        if (entity.getPublisherTwinClassId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty publisherTwinClassId");
        if (entity.getPublisherTwinAction() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty publisherTwinAction");
        return true;
    }

    public List<TwinClassFieldRecomputeOnActionEntity> findByPublisherTwinClassIdAndPublisherTwinAction(
            UUID publisherTwinClassId, TwinAction action) {
        if (publisherTwinClassId == null || action == null)
            return List.of();
        return repository.findByPublisherTwinClassIdAndPublisherTwinAction(publisherTwinClassId, action);
    }

    public void loadSubscriberTwinPointer(TwinClassFieldRecomputeOnActionEntity src) throws ServiceException {
        loadSubscriberTwinPointer(Collections.singleton(src));
    }

    public void loadSubscriberTwinPointer(Collection<TwinClassFieldRecomputeOnActionEntity> srcCollection) throws ServiceException {
        twinPointerService.load(srcCollection,
                TwinClassFieldRecomputeOnActionEntity::getSubscriberTwinPointerId,
                TwinClassFieldRecomputeOnActionEntity::getSubscriberTwinPointer,
                TwinClassFieldRecomputeOnActionEntity::setSubscriberTwinPointer);
    }

    public void loadSubscriberTwinClassField(TwinClassFieldRecomputeOnActionEntity src) throws ServiceException {
        loadSubscriberTwinClassField(Collections.singleton(src));
    }

    public void loadSubscriberTwinClassField(Collection<TwinClassFieldRecomputeOnActionEntity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                TwinClassFieldRecomputeOnActionEntity::getSubscriberTwinClassFieldId,
                TwinClassFieldRecomputeOnActionEntity::getSubscriberTwinClassField,
                TwinClassFieldRecomputeOnActionEntity::setSubscriberTwinClassField);
    }

    public void loadPublisherTwinClass(TwinClassFieldRecomputeOnActionEntity src) throws ServiceException {
        loadPublisherTwinClass(Collections.singleton(src));
    }

    public void loadPublisherTwinClass(Collection<TwinClassFieldRecomputeOnActionEntity> srcCollection) throws ServiceException {
        twinClassService.load(srcCollection,
                TwinClassFieldRecomputeOnActionEntity::getPublisherTwinClassId,
                TwinClassFieldRecomputeOnActionEntity::getPublisherTwinClass,
                TwinClassFieldRecomputeOnActionEntity::setPublisherTwinClass);
    }
}
