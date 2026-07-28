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
import org.twins.core.dao.recompute.TwinRecomputeSubscriberEntity;
import org.twins.core.dao.recompute.TwinRecomputeSubscriberRepository;
import org.twins.core.service.TwinsEntitySecureFindService;
import org.twins.core.service.twin.TwinPointerService;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Function;

/**
 * Secure-find service for {@link TwinRecomputeSubscriberEntity} — one recompute subscriber per
 * (twin pointer, twin class field), carrying the {@code Recomputer} featurer config.
 *
 * The {@code load*} methods hydrate the subscriber's twin pointer + twin class field in one batch SQL each
 * via the base {@link org.cambium.service.EntitySecureFindServiceImpl#load} on the related entity's service.
 */
@Lazy
@Slf4j
@Service
@LogExecutionTime(logPrefix = "LONG EXECUTION TIME:", logIfTookMoreThenMs = 2 * 1000, level = JavaLoggingLevel.WARNING)
@RequiredArgsConstructor
public class TwinRecomputeSubscriberService extends TwinsEntitySecureFindService<TwinRecomputeSubscriberEntity> {
    private final TwinRecomputeSubscriberRepository repository;
    @Lazy
    private final TwinPointerService twinPointerService;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

    @Override
    public CrudRepository<TwinRecomputeSubscriberEntity, UUID> entityRepository() {
        return repository;
    }

    @Override
    public Function<TwinRecomputeSubscriberEntity, UUID> entityGetIdFunction() {
        return TwinRecomputeSubscriberEntity::getId;
    }

    @Override
    public boolean isEntityReadDenied(TwinRecomputeSubscriberEntity entity, EntitySmartService.ReadPermissionCheckMode readPermissionCheckMode) throws ServiceException {
        return false;
    }

    @Override
    public boolean validateEntity(TwinRecomputeSubscriberEntity entity, EntitySmartService.EntityValidateMode entityValidateMode) throws ServiceException {
        if (entity.getId() == null)
            return logErrorAndReturnFalse(entity.logShort() + " empty id");
        if (entity.getDomainId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty domainId");
        if (entity.getSubscriberTwinPointerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty subscriberTwinPointerId");
        if (entity.getSubscriberTwinClassFieldId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty subscriberTwinClassFieldId");
        if (entity.getRecomputerFeaturerId() == null)
            return logErrorAndReturnFalse(entity.logNormal() + " empty recomputerFeaturerId");
        return true;
    }

    public void loadSubscriberTwinPointer(TwinRecomputeSubscriberEntity src) throws ServiceException {
        loadSubscriberTwinPointer(Collections.singleton(src));
    }

    public void loadSubscriberTwinPointer(Collection<TwinRecomputeSubscriberEntity> srcCollection) throws ServiceException {
        twinPointerService.load(srcCollection,
                TwinRecomputeSubscriberEntity::getSubscriberTwinPointerId,
                TwinRecomputeSubscriberEntity::getSubscriberTwinPointer,
                TwinRecomputeSubscriberEntity::setSubscriberTwinPointer);
    }

    public void loadSubscriberTwinClassField(TwinRecomputeSubscriberEntity src) throws ServiceException {
        loadSubscriberTwinClassField(Collections.singleton(src));
    }

    public void loadSubscriberTwinClassField(Collection<TwinRecomputeSubscriberEntity> srcCollection) throws ServiceException {
        twinClassFieldService.load(srcCollection,
                TwinRecomputeSubscriberEntity::getSubscriberTwinClassFieldId,
                TwinRecomputeSubscriberEntity::getSubscriberTwinClassField,
                TwinRecomputeSubscriberEntity::setSubscriberTwinClassField);
    }
}
